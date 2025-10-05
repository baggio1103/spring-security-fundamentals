package com.atomic.coding.config.filter

import com.atomic.coding.config.authentication.ApiKeyAuthentication
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiKeyAuthenticationFilter(
    private val authenticationManager: AuthenticationManager,
) : OncePerRequestFilter() {


    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader("X-API-Key")
        if (apiKey == null) {
            logger.info("ApiKey is null, processing in the filter chain further")
            filterChain.doFilter(request, response)
            return
        }
        try {
            val apiKeyAuthentication = authenticationManager.authenticate(ApiKeyAuthentication(apiKey, false))
            SecurityContextHolder.getContext().authentication = apiKeyAuthentication
            filterChain.doFilter(request, response)
        } catch (ex: AuthenticationException) {
            SecurityContextHolder.clearContext()
            logger.error("Exception occurred during authentication request: ${ex.message}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write("""{"error": "Authentication error"}""")
        }
    }

}