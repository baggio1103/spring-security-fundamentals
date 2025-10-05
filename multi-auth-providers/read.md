# 🔐 Spring Security, Part V - Implementing Multiple Authentication Providers

Welcome back to the Spring Security Series!

If you haven’t read the earlier tutorials, I recommend checking them out first — they lay the groundwork for what we’ll
build in this post.

---

## 💡 Scenario

Imagine you have a Spring Boot application secured with Basic Auth or Form Login. Now, you need to support an additional
mechanism — say, API Key Authentication.

How do you support both seamlessly, allowing clients to choose either method to authenticate?

In this tutorial, we’ll implement two authentication mechanisms:

- Basic Auth
- API Key Auth

This setup is especially useful when:

- External users access your API via Basic Auth.
- Internal services (within the same cluster) use API keys.

---

## ✅ What You’ll Learn

- How `AuthenticationManager` manages authentication
- How to implement a custom `AuthenticationManager` that supports multiple `AuthenticationProvider`s
- How to set up an `AuthenticationFilter` to support both types 
- How to build a full Spring Boot app supporting multiple authentication mechanisms

---

## 🔄 Authentication Flow in Spring Security

All authentication flows in Spring Security are filter-based. Filters are responsible for both authenticating
and authorizing incoming requests.

Here’s how it works:

1. When an HTTP request hits an endpoint, Spring Security applies a chain of filters before the request reaches your
   controller.
2. Depending on the authentication type, the appropriate filter is used.
    - For example, the `BasicAuthenticationFilter` handles Basic Auth requests.
3. The selected filter delegates authentication to the `AuthenticationManager`, which further delegates it to the
   appropriate `AuthenticationProvider`.

---

## 🛠️ What We’ll Implement

To support multiple authentication mechanisms, we need:

- `ApiKeyFilter`
- `ApiKeyAuthentication`
- `CustomAuthenticationManager`
- `ApiKeyAuthenticationProvider`

> Note:  
> `CustomAuthenticationManager` is an internal object managed by Spring. If you don’t define one explicitly, Spring
> provides a default. However, defining your own will override the default.  
> Only one `AuthenticationManager` can exist per `SecurityFilterChain`.

---

## 🏗 Project Setup

Here’s the `build.gradle.kts` file you'll need:

```kotlin
plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.atomic.coding"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
````

---

## 📦 Simple Controller for Testing

```kotlin
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    @GetMapping
    fun orders(): List<Order> {
        return orderService.orders()
    }
}
```

---

## 🔐 Default Spring Security Behavior

By default, Spring Security protects all routes using HTTP Basic authentication.

When you start the app and hit `/api/orders`, you’ll see:

```plaintext
Using generated security password: 8cbecab7-4d14-4c38-b9ca-2d072b8ba8f6
```

Use this default password to authenticate and you'll receive a `200 OK`.

---

## 🔑 Implementing Multi-Authentication

Let’s now implement a fully functional app supporting both API Key and Basic Auth.

---

### ✅ Step 1: Custom `Authentication` for API Key

To represent API key-based authentication, we create a class that implements Spring Security's Authenticationinterface.
Since we aren't using credentials, roles or user details, most of the interface methods will return either null or empty
collections for ApiKeyAuthentication.

```kotlin
package com.atomic.coding.config.authentication

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class ApiKeyAuthentication(
    val apiKey: String,
    private val authenticated: Boolean
) : Authentication {
    override fun getName(): String? = null
    override fun getAuthorities(): List<GrantedAuthority> = emptyList()
    override fun getCredentials(): Any? = null
    override fun getDetails(): Any? = null
    override fun getPrincipal(): Any? = null
    override fun isAuthenticated(): Boolean = authenticated
    override fun setAuthenticated(isAuthenticated: Boolean) {}
}
```

✅ This class acts as a wrapper for the API key and tracks whether the request is authenticated.

---

---

### ✅ 2. `ApiKeyAuthenticationProvider` – Verifying the API Key

Once a request reaches the authentication layer, Spring Security delegates the authentication task to
the `AuthenticationManager`, which in turn delegates it to an `AuthenticationProvider`.

Each `AuthenticationProvider` is responsible for handling a specific authentication mechanism.

Spring defines this interface as:

```java
public interface AuthenticationProvider {
    Authentication authenticate(Authentication authentication) throws AuthenticationException;

    boolean supports(Class<?> authentication);
}
```

In our case, we need a custom implementation to validate requests that use an API key. Our
custom `ApiKeyAuthenticationProvider` does two things:

* `authenticate(Authentication)` – Validates the API key against a pre-configured secret defined
  in `application.yml`.
* `supports(Class<?>)` – Indicates that this provider only supports `ApiKeyAuthentication` objects.

---

#### ✅ Code: `ApiKeyAuthenticationProvider`

```kotlin
package com.atomic.coding.config.provider

import com.atomic.coding.config.authentication.ApiKeyAuthentication
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class ApiKeyAuthenticationProvider(
    @Value("\${authentication.secret.api-key}")
    private val apiKey: String,
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication?): Authentication {
        val apiKeyAuthentication = authentication as ApiKeyAuthentication
        if (apiKeyAuthentication.apiKey == apiKey) {
            return ApiKeyAuthentication(apiKey, true)
        }
        throw BadCredentialsException("Invalid API key")
    }

    override fun supports(authentication: Class<*>?): Boolean =
        authentication == ApiKeyAuthentication::class.java
}
```

---

#### 🛠️ Configuration: `application.yml`

```yaml
spring:
  application:
    name: multi-providers

authentication:
  secret:
    api-key: some_secret_key
```

### ✅ 3. `DaoAuthenticationProvider` – Verifying Users Against the Database

In previous parts of this series, we discussed how `UserDetailsService` and `PasswordEncoder` work together to
authenticate users. When using username and password-based authentication (like Basic Auth or Form Login), Spring
Security relies on a built-in class called `DaoAuthenticationProvider`.

Since our application needs to support two authentication mechanisms (API Key and Basic Auth), we must explicitly
provide a `DaoAuthenticationProvider` to handle user authentication via username and password.

### 🔍 What is `DaoAuthenticationProvider`?

`DaoAuthenticationProvider` is a Spring Security implementation of the `AuthenticationProvider` interface. It performs
authentication by:

* Calling the provided `UserDetailsService` to load user details from memory or a database.
* Using the configured `PasswordEncoder` to verify passwords.

> Normally, Spring configures this automatically. But because we're providing a custom `AuthenticationManager`, we
> need to manually declare and register the `DaoAuthenticationProvider`.

### ✅ Code: Configuring `DaoAuthenticationProvider`

```kotlin
@Configuration
class DaoAuthenticationProviderConfig {

    @Bean
    fun inMemoryUserDetailsManager(): UserDetailsService {
        val user = User.builder()
            .username("alice")
            .password(passwordEncoder().encode("qwerty"))
            .roles("USER")
            .build()
        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun daoAuthenticationProvider(userDetailsService: UserDetailsService): DaoAuthenticationProvider =
        DaoAuthenticationProvider(userDetailsService)
}
```

### 🧠 Explanation:

* `UserDetailsService`: For simplicity, we use `InMemoryUserDetailsManager` to simulate a user store.
* `PasswordEncoder`: We use `BCryptPasswordEncoder`, a secure password hashing algorithm.
* `DaoAuthenticationProvider`: Combines both to validate credentials during Basic Auth.

------------------------------------------------------------------------------------------------------------------------


Here's a polished and refined version of Section 4: CustomAuthenticationManager, with improved grammar, structure,
clarity, and consistency — while preserving all technical details.

---

### ✅ 4. `CustomAuthenticationManager` – Coordinating Authentication

The `CustomAuthenticationManager` is the core component that coordinates which `AuthenticationProvider` should
handle each authentication request.

Since our application supports two different authentication mechanisms (API Key and Basic Auth), we need a manager
that:

1. Identifies which authentication type is being used.
2. Delegates the request to the correct `AuthenticationProvider`.
3. Throws an error if no provider supports the given authentication type.

---

### 🔧 Implementation

```kotlin
package com.atomic.coding.config.manager

import com.atomic.coding.config.provider.ApiKeyAuthenticationProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationManager(
    private val apiKeyAuthenticationProvider: ApiKeyAuthenticationProvider,
    private val daoAuthenticationProvider: DaoAuthenticationProvider,
) : AuthenticationManager {

    override fun authenticate(authentication: Authentication): Authentication = when {
        apiKeyAuthenticationProvider.supports(authentication::class.java) ->
            apiKeyAuthenticationProvider.authenticate(authentication)

        daoAuthenticationProvider.supports(authentication::class.java) ->
            daoAuthenticationProvider.authenticate(authentication)

        else -> throw BadCredentialsException("Unsupported authentication type: ${authentication.javaClass.simpleName}")
    }
}
```

### 🧠 Explanation

* The `authenticate()` method is invoked for every request that requires authentication.
* It checks:

    * If the request is of type `ApiKeyAuthentication`, it’s delegated to `ApiKeyAuthenticationProvider`.
    * If the request is of type `UsernamePasswordAuthenticationToken` (used by Basic Auth), it’s handled
      by `DaoAuthenticationProvider`.
* If no provider supports the authentication type, a `BadCredentialsException` is thrown, resulting in a 401
  Unauthorized response.

📌 Why use a custom `AuthenticationManager`?

By default, Spring Security auto-configures its own `AuthenticationManager`. But when we introduce multiple custom
providers, we take full control of the decision-making process with a custom implementation like this.

### ✅ 5. `ApiKeyAuthenticationFilter` – Intercepting HTTP Requests

As previously discussed, Spring Security is filter-based. Every incoming request flows through a chain of filters
, each responsible for handling a specific security concern.

In our case, we introduce a custom filter, `ApiKeyAuthenticationFilter`, to handle API Key-based authentication.

---

### 🔧 Implementation

```kotlin
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
        val apiKey = request.getHeader("X-Api-Key")

        // If no API key is provided, continue the filter chain.
        if (apiKey == null) {
            logger.info("API key is missing, passing request to next filter")
            filterChain.doFilter(request, response)
            return
        }

        try {
            // Attempt API key authentication
            val apiKeyAuthentication = authenticationManager.authenticate(ApiKeyAuthentication(apiKey, false))
            SecurityContextHolder.getContext().authentication = apiKeyAuthentication
            filterChain.doFilter(request, response)

        } catch (ex: AuthenticationException) {
            // Authentication failed
            SecurityContextHolder.clearContext()
            logger.error("Authentication failed: ${ex.message}")

            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write("""{"error": "Authentication error"}""")
        }
    }
}
```

### 🔍 What This Filter Does

Here's a breakdown of its responsibilities:

1. Extracts the API key from the `X-Api-Key` HTTP header.
2. If no API key is found:

    * It simply passes the request along the filter chain.
    * This allows other filters (like the `BasicAuthenticationFilter`) to handle the authentication — enabling multiple
      auth mechanisms to coexist.
3. If an API key is provided:

    * It creates an `ApiKeyAuthentication` object.
    * It delegates authentication to the `AuthenticationManager`.
    * On success, it sets the authenticated object in the `SecurityContext`.
    * On failure, it responds with `401 Unauthorized` and a simple JSON error message.

### ✅ Step 4: Custom Authentication Manager

```kotlin
package com.atomic.coding.config.manager

import com.atomic.coding.config.provider.ApiKeyAuthenticationProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationManager(
    private val apiKeyAuthenticationProvider: ApiKeyAuthenticationProvider,
    private val daoAuthenticationProvider: DaoAuthenticationProvider,
) : AuthenticationManager {

    override fun authenticate(authentication: Authentication): Authentication = when {
        apiKeyAuthenticationProvider.supports(authentication::class.java) ->
            apiKeyAuthenticationProvider.authenticate(authentication)

        daoAuthenticationProvider.supports(authentication::class.java) ->
            daoAuthenticationProvider.authenticate(authentication)

        else -> throw BadCredentialsException("Unsupported authentication type: ${authentication.javaClass.simpleName}")
    }
}
```

### ✅ Step 5: API Key Authentication Filter

```kotlin
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
        val apiKey = request.getHeader("X-Api-Key")
        if (apiKey == null) {
            logger.info("API key is missing, proceeding with the next filter")
            filterChain.doFilter(request, response)
            return
        }

        try {
            val apiKeyAuthentication = authenticationManager.authenticate(ApiKeyAuthentication(apiKey, false))
            SecurityContextHolder.getContext().authentication = apiKeyAuthentication
            filterChain.doFilter(request, response)
        } catch (ex: AuthenticationException) {
            SecurityContextHolder.clearContext()
            logger.error("Authentication failed: ${ex.message}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write("""{"error": "Authentication error"}""")
        }
    }
}
```

### ✅ 6. `SecurityConfig` – Registering the Filter

The final step is to wire everything together by registering our custom filter within the Spring Security filter
chain.

We ensure that our `ApiKeyAuthenticationFilter` is evaluated before Spring's built-in `BasicAuthenticationFilter`.
This guarantees that API key authentication is attempted first, but doesn't block Basic Auth if no API key is
provided.

### 🛠 Implementation

```kotlin
package com.atomic.coding.config

import com.atomic.coding.config.filter.ApiKeyAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
class SecurityConfig(
    private val apiKeyAuthenticationFilter: ApiKeyAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .addFilterBefore(apiKeyAuthenticationFilter, BasicAuthenticationFilter::class.java)
        .httpBasic { } // Enable Basic Auth
        .authorizeHttpRequests { it.anyRequest().authenticated() } // Require auth for all endpoints
        .build()
}
```

### 📌 Key Notes

* We explicitly call `.httpBasic { }` to enable Basic Authentication.
* The `.addFilterBefore(...)` method inserts our custom `ApiKeyAuthenticationFilter` before the
  built-in `BasicAuthenticationFilter`.
* All incoming HTTP requests are now required to be authenticated, whether via:

    * `X-API-Key` header, or
    * Basic Auth credentials.
---

### ✅ Result

With this setup:

* 🔒 All endpoints are secured.
* ✅ Valid API keys or Basic Auth credentials will be accepted.
* ❌ Any unauthenticated request will result in a `401 Unauthorized` or `403 Forbidden`, depending on context.

You now have a fully working multi-authentication setup in Spring Security! 🙌

Let me know if you’d like a follow-up snippet showing role-based access or endpoint exclusions.


---

## 🧪 Testing the Setup

Use Postman, curl, or HTTPie to test:

| Request Type                        | Result             |
|-------------------------------------|--------------------|
| No auth                             | ❌ 403 Forbidden    |
| Valid Basic Auth credentials        | ✅ 200 OK           |
| Invalid Basic Auth credentials      | ❌ 401 Unauthorized |
| Valid API Key in `X-API-Key` header | ✅ 200 OK           |
| Invalid API Key                     | ❌ 401 Unauthorized |

---

## 🔁 Recap

Here’s what we built:

- ✅ `ApiKeyAuthentication` – Custom `Authentication`
- ✅ `ApiKeyAuthenticationProvider` – Custom `AuthenticationProvider`
- ✅ `CustomAuthenticationManager` – Supports both Basic Auth & API Key
- ✅ `ApiKeyAuthenticationFilter` – Custom `OncePerRequestFilter`
- ✅ `SecurityConfig` – Integrated the filter chain

---

## 🔮 What’s Next?

In the next part of this series, we’ll dive into:

- 🔐 Authorization Strategies
    - Authority vs Role
    - Entry-point level
    - Method-level security

Stay tuned! 🚀

---

If you found this guide helpful, consider liking, sharing, or commenting below.  
Thanks for reading — see you in the next part! 👋