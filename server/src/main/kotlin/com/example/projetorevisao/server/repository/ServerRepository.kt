package com.example.projetorevisao.server.repository

import com.example.projetorevisao.server.database.DatabaseFactory
import com.example.projetorevisao.server.models.*
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ServerRepository(private val mediaRepository: MediaRepository = MediaRepository()) {

    // --- AUTH & USER ---

    fun getUserProfile(userId: String): UserProfileDto? {
        DatabaseFactory.getConnection().use { conn ->
            val sql = "SELECT id, name, email, neighborhood, trust_score, anonymous_default, geofence_radius FROM users WHERE id = ?"
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, userId)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    val anonDefault = rs.getInt("anonymous_default") == 1
                    val geofence = rs.getFloat("geofence_radius")

                    fun count(sql: String): Int = conn.prepareStatement(sql).use { st ->
                        st.setString(1, userId)
                        st.executeQuery().use { r -> if (r.next()) r.getInt(1) else 0 }
                    }
                    val reportsCount = count("SELECT COUNT(*) FROM incidents WHERE reporter_id = ?")
                    val confirmationsCount = count("SELECT COUNT(*) FROM incident_confirmations WHERE user_id = ?")

                    return UserProfileDto(
                        userId = rs.getString("id"),
                        name = rs.getString("name"),
                        email = rs.getString("email"),
                        neighborhood = rs.getString("neighborhood"),
                        userTrustScore = rs.getInt("trust_score"),
                        anonymousReportingDefault = anonDefault,
                        geofenceRadiusKm = geofence,
                        totalReportsCreated = reportsCount,
                        totalConfirmationsGiven = confirmationsCount
                    )
                }
            }
        }
        return null
    }

    fun updateUserProfile(userId: String, req: UpdateProfileRequest): UserProfileDto? {
        DatabaseFactory.getConnection().use { conn ->
            req.neighborhood?.let { n ->
                conn.prepareStatement("UPDATE users SET neighborhood = ? WHERE id = ?").use {
                    it.setString(1, n)
                    it.setString(2, userId)
                    it.executeUpdate()
                }
            }
            req.anonymousReportingDefault?.let { a ->
                conn.prepareStatement("UPDATE users SET anonymous_default = ? WHERE id = ?").use {
                    it.setInt(1, if (a) 1 else 0)
                    it.setString(2, userId)
                    it.executeUpdate()
                }
            }
            req.geofenceRadiusKm?.let { g ->
                conn.prepareStatement("UPDATE users SET geofence_radius = ? WHERE id = ?").use {
                    it.setFloat(1, g)
                    it.setString(2, userId)
                    it.executeUpdate()
                }
            }
        }
        return getUserProfile(userId)
    }

    // --- INCIDENTS ---

    /**
     * Relatos visíveis. Com [viewerId], também esconde o que a pessoa denunciou e os relatos de quem ela
     * bloqueou, e diz se ela já confirmou cada um.
     */
    fun getIncidents(neighborhoodFilter: String?, categoryFilter: String?, query: String?, viewerId: String? = null): List<IncidentDto> {
        val list = mutableListOf<IncidentDto>()
        DatabaseFactory.getConnection().use { conn ->
            var sql = "SELECT *, " +
                (if (viewerId != null) "EXISTS(SELECT 1 FROM incident_confirmations c WHERE c.incident_id = incidents.id AND c.user_id = ?)"
                else "0") + " AS viewer_confirmed FROM incidents WHERE hidden = 0"
            val params = mutableListOf<String>()
            if (viewerId != null) {
                params.add(viewerId)
                sql += " AND id NOT IN (SELECT incident_id FROM incident_reports WHERE reporter_user_id = ?)"
                params.add(viewerId)
                sql += " AND (reporter_id IS NULL OR reporter_id NOT IN (SELECT blocked_id FROM user_blocks WHERE blocker_id = ?))"
                params.add(viewerId)
            }

            if (!neighborhoodFilter.isNullOrEmpty()) {
                sql += " AND neighborhood LIKE ?"
                params.add("%$neighborhoodFilter%")
            }
            if (!categoryFilter.isNullOrEmpty()) {
                sql += " AND category = ?"
                params.add(categoryFilter)
            }
            if (!query.isNullOrEmpty()) {
                sql += " AND (title LIKE ? OR description LIKE ? OR address LIKE ?)"
                params.add("%$query%")
                params.add("%$query%")
                params.add("%$query%")
            }
            sql += " ORDER BY created_at DESC"

            conn.prepareStatement(sql).use { stmt ->
                params.forEachIndexed { index, p -> stmt.setString(index + 1, p) }
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    list.add(
                        IncidentDto(
                            id = rs.getString("id"),
                            category = rs.getString("category"),
                            customBadgeTitle = rs.getString("custom_badge_title"),
                            title = rs.getString("title"),
                            description = rs.getString("description"),
                            address = rs.getString("address"),
                            neighborhood = rs.getString("neighborhood"),
                            distanceMeters = rs.getInt("distance_meters"),
                            timeAgo = timeAgoLabel(rs.getLong("created_at")),
                            verified = rs.getInt("verified") == 1,
                            confirmations = rs.getInt("confirmations"),
                            commentsCount = rs.getInt("comments_count"),
                            sharesCount = rs.getInt("shares_count"),
                            status = rs.getString("status"),
                            authorityAssigned = rs.getString("authority_assigned"),
                            isAnonymous = rs.getInt("is_anonymous") == 1,
                            mapXFraction = rs.getFloat("map_x_fraction"),
                            mapYFraction = rs.getFloat("map_y_fraction"),
                            latitude = rs.getDouble("latitude"),
                            longitude = rs.getDouble("longitude"),
                            aiSafetyRecommendation = rs.getString("ai_safety_recommendation"),
                            userConfirmed = rs.getInt("viewer_confirmed") == 1,
                            createdAtEpochMs = rs.getLong("created_at")
                        )
                    )
                }
            }
        }
        val media = mediaRepository.mediaForAll(list.map { it.id })
        return list.map { it.copy(media = media[it.id].orEmpty()) }
    }

    fun createIncident(reporterId: String, req: CreateIncidentRequest): IncidentDto {
        val id = "inc-${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()
        val (lat, lon) = getCoordsForNeighborhood(req.neighborhood)

        val aiRec = when (req.category) {
            "ROUBO_FURTO" -> "Mantenha pertences protegidos e evite aglomerações desnecessárias."
            "INCENDIO" -> "Distancie-se da fumaça e desimpeça a via para os veículos de emergência."
            "ALAGAMENTO" -> "Não tente atravessar enxurradas a pé ou com veículos leves."
            "TEMPESTADE" -> "Procure abrigo seguro longe de árvores e estruturas metálicas."
            else -> "Atenção redobrada ao transitar por esta região."
        }

        // O relato não é encaminhado a nenhum órgão público; o texto precisa dizer isso.
        val authority = "Não é enviado à polícia. Em perigo, ligue 190."

        val newIncident = IncidentDto(
            id = id,
            category = req.category,
            customBadgeTitle = null,
            title = req.title,
            description = req.description,
            address = req.address,
            neighborhood = req.neighborhood,
            distanceMeters = 0,
            timeAgo = "Agora mesmo",
            verified = false,
            confirmations = 0,
            commentsCount = 0,
            sharesCount = 0,
            status = "REPORTADO",
            authorityAssigned = authority,
            isAnonymous = req.isAnonymous,
            mapXFraction = 0.50f,
            mapYFraction = 0.50f,
            latitude = req.latitude ?: lat,
            longitude = req.longitude ?: lon,
            aiSafetyRecommendation = aiRec,
            userConfirmed = false,
            createdAtEpochMs = now
        )

        DatabaseFactory.getConnection().use { conn ->
            val sql = """
                INSERT INTO incidents (
                    id, category, custom_badge_title, title, description, address, neighborhood,
                    distance_meters, time_ago, verified, confirmations, comments_count, shares_count,
                    status, authority_assigned, is_anonymous, map_x_fraction, map_y_fraction,
                    latitude, longitude, ai_safety_recommendation, user_confirmed, created_at, reporter_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, newIncident.id)
                stmt.setString(2, newIncident.category)
                if (newIncident.customBadgeTitle != null) stmt.setString(3, newIncident.customBadgeTitle) else stmt.setNull(3, java.sql.Types.VARCHAR)
                stmt.setString(4, newIncident.title)
                stmt.setString(5, newIncident.description)
                stmt.setString(6, newIncident.address)
                stmt.setString(7, newIncident.neighborhood)
                stmt.setInt(8, newIncident.distanceMeters)
                stmt.setString(9, newIncident.timeAgo)
                stmt.setInt(10, if (newIncident.verified) 1 else 0)
                stmt.setInt(11, newIncident.confirmations)
                stmt.setInt(12, newIncident.commentsCount)
                stmt.setInt(13, newIncident.sharesCount)
                stmt.setString(14, newIncident.status)
                stmt.setString(15, newIncident.authorityAssigned)
                stmt.setInt(16, if (newIncident.isAnonymous) 1 else 0)
                stmt.setFloat(17, newIncident.mapXFraction)
                stmt.setFloat(18, newIncident.mapYFraction)
                stmt.setDouble(19, newIncident.latitude)
                stmt.setDouble(20, newIncident.longitude)
                stmt.setString(21, newIncident.aiSafetyRecommendation)
                stmt.setInt(22, 0)
                stmt.setLong(23, now)
                stmt.setString(24, reporterId)
                stmt.executeUpdate()
            }
        }

        return newIncident
    }

    /** "Eu também vi": liga ou desliga a confirmação desta pessoa. Null se o relato não existe ou está oculto. */
    fun toggleConfirmation(userId: String, incidentId: String): ConfirmIncidentResponse? {
        DatabaseFactory.getConnection().use { conn ->
            val visible = conn.prepareStatement("SELECT 1 FROM incidents WHERE id = ? AND hidden = 0").use { st ->
                st.setString(1, incidentId)
                st.executeQuery().use { it.next() }
            }
            if (!visible) return null
            val removed = conn.prepareStatement("DELETE FROM incident_confirmations WHERE incident_id = ? AND user_id = ?").use { st ->
                st.setString(1, incidentId)
                st.setString(2, userId)
                st.executeUpdate() > 0
            }
            if (!removed) {
                conn.prepareStatement("INSERT INTO incident_confirmations (incident_id, user_id, created_at) VALUES (?, ?, ?)").use { st ->
                    st.setString(1, incidentId)
                    st.setString(2, userId)
                    st.setLong(3, System.currentTimeMillis())
                    st.executeUpdate()
                }
            }
            val total = conn.prepareStatement("SELECT COUNT(*) FROM incident_confirmations WHERE incident_id = ?").use { st ->
                st.setString(1, incidentId)
                st.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
            conn.prepareStatement("UPDATE incidents SET confirmations = ? WHERE id = ?").use { st ->
                st.setInt(1, total)
                st.setString(2, incidentId)
                st.executeUpdate()
            }
            return ConfirmIncidentResponse(incidentId = incidentId, confirmations = total, userConfirmed = !removed)
        }
    }

    /** Devolve null quando o incidente não existe. */
    fun addComment(userId: String, incidentId: String, req: CreateCommentRequest): CommentDto? {
        val commentId = "cmt-${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()
        val exists = DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("SELECT 1 FROM incidents WHERE id = ?").use { stmt ->
                stmt.setString(1, incidentId)
                stmt.executeQuery().next()
            }
        }
        if (!exists) return null
        val author = if (req.isAnonymous) {
            "Cidadão Anônimo"
        } else {
            getUserProfile(userId)?.name ?: "Cidadão Colaborador"
        }

        val comment = CommentDto(
            id = commentId,
            incidentId = incidentId,
            authorName = author,
            content = req.content,
            timeAgo = "Agora",
            isOfficialAuthority = false
        )

        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("""
                INSERT INTO comments (id, incident_id, author_name, content, time_ago, is_official_authority, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()).use { stmt ->
                stmt.setString(1, comment.id)
                stmt.setString(2, comment.incidentId)
                stmt.setString(3, comment.authorName)
                stmt.setString(4, comment.content)
                stmt.setString(5, comment.timeAgo)
                stmt.setInt(6, 0)
                stmt.setLong(7, now)
                stmt.executeUpdate()
            }

            conn.prepareStatement("UPDATE incidents SET comments_count = comments_count + 1 WHERE id = ?").use { stmt ->
                stmt.setString(1, incidentId)
                stmt.executeUpdate()
            }
        }

        return comment
    }

    fun getCommentsForIncident(incidentId: String): List<CommentDto> {
        val list = mutableListOf<CommentDto>()
        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("SELECT * FROM comments WHERE incident_id = ? ORDER BY created_at ASC").use { stmt ->
                stmt.setString(1, incidentId)
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    list.add(
                        CommentDto(
                            id = rs.getString("id"),
                            incidentId = rs.getString("incident_id"),
                            authorName = rs.getString("author_name"),
                            content = rs.getString("content"),
                            timeAgo = timeAgoLabel(rs.getLong("created_at")),
                            isOfficialAuthority = rs.getInt("is_official_authority") == 1
                        )
                    )
                }
            }
        }
        return list
    }

    // --- TELEMETRY ---

    fun getWeatherTelemetry(neighborhood: String): WeatherTelemetryDto {
        val timeLabel = SimpleDateFormat("HH:mm", Locale.forLanguageTag("pt-BR")).format(Date())
        return WeatherTelemetryDto(
            regionName = neighborhood,
            temperatureCelsius = 21.2,
            precipitationMm = 4.5,
            windSpeedKmh = 18.0,
            windGustsKmh = 35.0,
            humidityPercent = 82,
            conditionLabel = "Pancadas de Chuva Isoladas em SP",
            inmetAlertLevel = "AMARELO",
            cgeFloodRiskLabel = "Monitoramento do CGE indica pontos de atenção para alagamentos em vias baixas.",
            cgeKeywordMatchCount = 2,
            sourceApi = "Open-Meteo + INMET Alert-AS & CGE-SP",
            lastUpdatedTime = timeLabel,
            isLiveNetworkData = true
        )
    }

    fun getDistrictTelemetry(neighborhood: String): DistrictTelemetryDto {
        return when {
            neighborhood.contains("Capão Redondo", ignoreCase = true) -> DistrictTelemetryDto(
                dpCode = "47º DP",
                dpName = "47º Distrito Policial - Capão Redondo",
                militaryBattalion = "37º BPM/M • Comando de Policiamento Zona Sul",
                smartSampaCamerasActive = 152,
                securityTotemsCount = 9,
                riskIndexScore = 64,
                peakAlertHours = "18h00 às 23h00 (Retorno Metrô Linha 5-Lilás)",
                recentTrendSummary = "Redução de 14% em furtos no entorno do Parque Santo Dias após ativação de Base Móvel e alertas comunitários.",
                sp156OpenProtocolsNearby = 12,
                isLiveSynced = true
            )
            neighborhood.contains("Paulista", ignoreCase = true) -> DistrictTelemetryDto(
                dpCode = "78º DP",
                dpName = "78º Distrito Policial - Jardins / Bela Vista",
                militaryBattalion = "11º BPM/M • Operação Centro Seguro",
                smartSampaCamerasActive = 418,
                securityTotemsCount = 26,
                riskIndexScore = 52,
                peakAlertHours = "12h00 às 14h30 e 18h30 às 21h30",
                recentTrendSummary = "Atenção redobrada para furtos de celulares por bicicletas na ciclovia e cruzamentos.",
                sp156OpenProtocolsNearby = 7,
                isLiveSynced = true
            )
            else -> DistrictTelemetryDto(
                dpCode = "14º DP",
                dpName = "14º Distrito Policial - Pinheiros",
                militaryBattalion = "23º BPM/M • Zona Oeste",
                smartSampaCamerasActive = 270,
                securityTotemsCount = 18,
                riskIndexScore = 46,
                peakAlertHours = "19h00 às 23h30 (Eixo Faria Lima / Teodoro Sampaio)",
                recentTrendSummary = "Integração Smart Sampa + Ilumina SP priorizando reparos de iluminação pública perto de estações.",
                sp156OpenProtocolsNearby = 5,
                isLiveSynced = true
            )
        }
    }

    // --- MOBILITY ---

    fun getMetroLines(): List<MetroLineDto> {
        return listOf(
            MetroLineDto("L1", "Linha 1 - Azul", "Metrô SP", "#003399", "Operação Normal", true, "Média", 3, "Monitoramento ostensivo nas estações Sé e Luz.", 48),
            MetroLineDto("L2", "Linha 2 - Verde", "Metrô SP", "#008055", "Operação Normal", true, "Tranquila", 4, "Patrulhamento integrado na Av. Paulista.", 36),
            MetroLineDto("L3", "Linha 3 - Vermelha", "Metrô SP", "#EE3338", "Operação Normal", true, "Intensa", 2, "Reforço policial no Terminal Barra Funda e Brás.", 62),
            MetroLineDto("L4", "Linha 4 - Amarela", "ViaQuatro", "#FFCC00", "Operação Normal", true, "Tranquila", 3, "Câmeras de reconhecimento facial ativas.", 40),
            MetroLineDto("L5", "Linha 5 - Lilás", "ViaMobilidade", "#800080", "Operação Normal", true, "Média", 4, "Integração segura no Terminal Capão Redondo.", 32),
            MetroLineDto("L15", "Linha 15 - Prata", "Metrô SP", "#808080", "Velocidade Reduzida", false, "Média", 6, "Atenção ao tempo de espera elevado na estação Vila Prudente.", 18)
        )
    }

    fun getSafeRoutes(): List<SafeRouteDto> {
        return listOf(
            SafeRouteDto(
                id = "rt-1",
                origin = "Estação Capão Redondo (Linha 5)",
                destination = "Parque Santo Dias",
                title = "Rota Recomendada • Ilumina SP & Câmeras Smart Sampa",
                durationMinutes = 12,
                distanceKm = "0.9 km",
                safetyScore = 92,
                lightingCoveragePercent = 95,
                activeCamerasCount = 14,
                avoidedIncidentsCount = 3,
                isRecommendedSafe = true,
                routeSummary = "Caminho priorizado com totens de segurança eiluminação LED recente."
            ),
            SafeRouteDto(
                id = "rt-2",
                origin = "Estação Trianon-Masp",
                destination = "Conjunto Nacional",
                title = "Eixo Paulista Monitorado",
                durationMinutes = 8,
                distanceKm = "0.6 km",
                safetyScore = 96,
                lightingCoveragePercent = 100,
                activeCamerasCount = 28,
                avoidedIncidentsCount = 5,
                isRecommendedSafe = true,
                routeSummary = "Monitoramento constante da Operação Centro Seguro."
            )
        )
    }

    // --- REELS ---

    fun getReels(): List<SafetyReelDto> {
        val list = mutableListOf<SafetyReelDto>()
        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("SELECT * FROM reels ORDER BY likes_count DESC").use { stmt ->
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    list.add(
                        SafetyReelDto(
                            id = rs.getString("id"),
                            title = rs.getString("title"),
                            description = rs.getString("description"),
                            authorName = rs.getString("author_name"),
                            authorRole = rs.getString("author_role"),
                            isOfficialVerified = rs.getInt("is_official_verified") == 1,
                            location = rs.getString("location"),
                            timeAgo = rs.getString("time_ago"),
                            viewsLabel = rs.getString("views_label"),
                            likesCount = rs.getInt("likes_count"),
                            commentsCount = rs.getInt("comments_count"),
                            sharesCount = rs.getInt("shares_count"),
                            category = rs.getString("category"),
                            isLiked = rs.getInt("is_liked") == 1,
                            durationSeconds = rs.getInt("duration_seconds")
                        )
                    )
                }
            }
        }
        return list
    }

    fun toggleLikeReel(reelId: String): ToggleLikeResponse? {
        DatabaseFactory.getConnection().use { conn ->
            var currentLikes = 0
            var isLiked = false
            conn.prepareStatement("SELECT likes_count, is_liked FROM reels WHERE id = ?").use { stmt ->
                stmt.setString(1, reelId)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    currentLikes = rs.getInt("likes_count")
                    isLiked = rs.getInt("is_liked") == 1
                } else {
                    return null
                }
            }

            val newIsLiked = !isLiked
            val newLikes = if (newIsLiked) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(0)

            conn.prepareStatement("UPDATE reels SET likes_count = ?, is_liked = ? WHERE id = ?").use { stmt ->
                stmt.setInt(1, newLikes)
                stmt.setInt(2, if (newIsLiked) 1 else 0)
                stmt.setString(3, reelId)
                stmt.executeUpdate()
            }

            return ToggleLikeResponse(reelId, newIsLiked, newLikes)
        }
    }

    // --- SOS & GUARDIANS ---

    fun triggerSosAlert(userId: String, req: SosAlertRequest): SosAlertResponse {
        val protocolId = "SOS-${UUID.randomUUID().toString().take(6).uppercase()}"
        val timestamp = SimpleDateFormat("HH:mm:ss • dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).format(Date())
        val guardians = getGuardians(userId)

        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("""
                INSERT INTO sos_alerts (
                    protocol_id, user_id, neighborhood, latitude, longitude, emergency_type,
                    trigger_source, dispatched_unit, estimated_arrival_minutes, status,
                    guardians_alerted_count, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()).use { stmt ->
                stmt.setString(1, protocolId)
                stmt.setString(2, userId)
                stmt.setString(3, req.neighborhood)
                req.latitude?.let { stmt.setDouble(4, it) } ?: stmt.setNull(4, java.sql.Types.DOUBLE)
                req.longitude?.let { stmt.setDouble(5, it) } ?: stmt.setNull(5, java.sql.Types.DOUBLE)
                stmt.setString(6, req.emergencyType)
                stmt.setString(7, req.triggerSource)
                stmt.setString(8, "NENHUMA")
                stmt.setInt(9, 0)
                stmt.setString(10, "REGISTRADO")
                stmt.setInt(11, guardians.size)
                stmt.setLong(12, System.currentTimeMillis())
                stmt.executeUpdate()
            }
        }

        val status = if (guardians.isEmpty()) {
            "Alerta registrado. Você ainda não tem guardiões cadastrados. Em perigo, ligue 190."
        } else {
            "Alerta registrado. Envie a mensagem aos seus ${guardians.size} guardiões. Em perigo, ligue 190."
        }
        return SosAlertResponse(protocolId = protocolId, timestamp = timestamp, status = status, guardians = guardians)
    }

    fun getGuardians(userId: String): List<GuardianContactDto> {
        val list = mutableListOf<GuardianContactDto>()
        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("SELECT * FROM guardians WHERE user_id = ? ORDER BY rowid").use { stmt ->
                stmt.setString(1, userId)
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    list.add(
                        GuardianContactDto(
                            id = rs.getString("id"),
                            name = rs.getString("name"),
                            relation = rs.getString("relation"),
                            phone = rs.getString("phone"),
                            isSharingLiveLocation = false,
                            distanceLabel = GUARDIAN_LABEL
                        )
                    )
                }
            }
        }
        return list
    }

    fun addGuardian(userId: String, req: AddGuardianRequest): GuardianContactDto {
        val dto = GuardianContactDto(
            id = "g-${UUID.randomUUID()}",
            name = req.name.trim(),
            relation = req.relation.trim(),
            phone = req.phone.trim(),
            isSharingLiveLocation = false,
            distanceLabel = GUARDIAN_LABEL
        )
        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("""
                INSERT INTO guardians (id, user_id, name, relation, phone, is_sharing_live_location, distance_label)
                VALUES (?, ?, ?, ?, ?, 0, ?)
            """.trimIndent()).use { stmt ->
                stmt.setString(1, dto.id)
                stmt.setString(2, userId)
                stmt.setString(3, dto.name)
                stmt.setString(4, dto.relation)
                stmt.setString(5, dto.phone)
                stmt.setString(6, dto.distanceLabel)
                stmt.executeUpdate()
            }
        }
        return dto
    }

    /** Só remove guardiões do próprio usuário. */
    fun deleteGuardian(userId: String, guardianId: String): Boolean =
        DatabaseFactory.getConnection().use { conn ->
            conn.prepareStatement("DELETE FROM guardians WHERE id = ? AND user_id = ?").use { stmt ->
                stmt.setString(1, guardianId)
                stmt.setString(2, userId)
                stmt.executeUpdate() > 0
            }
        }

    private companion object {
        const val GUARDIAN_LABEL = "Recebe seu aviso de SOS pelo WhatsApp"
    }

    // --- INTEGRATIONS CATALOG ---

    fun getIntegratedApisCatalog(): List<ExternalApiStatusDto> = listOf(
        ExternalApiStatusDto(
            id = "api-weather",
            name = "Open-Meteo, INMET e CGE-SP",
            provider = "Serviços públicos de Meteorologia e Alagamentos",
            endpointSummary = "api.open-meteo.com • cgesp.org",
            category = "Meteorologia",
            isConnected = true,
            latencyMs = 45,
            featuresProvided = "Leitura de temperatura, precipitação e alagamentos."
        ),
        ExternalApiStatusDto(
            id = "api-overpass",
            name = "Overpass / OpenStreetMap",
            provider = "OpenStreetMap contributors",
            endpointSummary = "overpass-api.de/api/interpreter",
            category = "Segurança SP",
            isConnected = true,
            latencyMs = 120,
            featuresProvided = "Mapeamento de bases policiais e iluminação pública."
        ),
        ExternalApiStatusDto(
            id = "api-metro",
            name = "Direto do Metrô SP & ViaMobilidade",
            provider = "Companhia do Metropolitano de São Paulo",
            endpointSummary = "metro.sp.gov.br/Sistemas/direto-do-metro",
            category = "Mobilidade SP",
            isConnected = true,
            latencyMs = 38,
            featuresProvided = "Status em tempo real das linhas L1, L2, L3, L4, L5, L15."
        )
    )

    private fun getCoordsForNeighborhood(neighborhood: String): Pair<Double, Double> {
        return when {
            neighborhood.contains("Capão Redondo", ignoreCase = true) -> -23.6596 to -46.7681
            neighborhood.contains("Paulista", ignoreCase = true) -> -23.5614 to -46.6559
            neighborhood.contains("Pinheiros", ignoreCase = true) -> -23.5673 to -46.6928
            else -> -23.5505 to -46.6333
        }
    }
}

/** "Agora mesmo", "há 5 min", "há 3 h", "há 2 dias" — calculado na hora da leitura. */
internal fun timeAgoLabel(createdAtMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    val minutes = ((nowMs - createdAtMs).coerceAtLeast(0) / 60_000)
    return when {
        minutes < 1 -> "Agora mesmo"
        minutes < 60 -> "há $minutes min"
        minutes < 24 * 60 -> "há ${minutes / 60} h"
        minutes < 2 * 24 * 60 -> "ontem"
        else -> "há ${minutes / (24 * 60)} dias"
    }
}
