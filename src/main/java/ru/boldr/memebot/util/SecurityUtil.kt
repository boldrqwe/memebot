package ru.boldr.memebot.util

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails


class SecurityUtil {
    companion object {
        fun getCurrentUserDetails(): UserDetails? {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated) {
                val principal = authentication.principal
                if (principal is UserDetails) {
                    return principal
                }
            }
            return null
        }

        fun getCurrentUsername(): String? {
            val authentication = SecurityContextHolder.getContext().authentication
            return if (authentication != null && authentication.isAuthenticated) {
                authentication.name
            } else null
        }
    }
}