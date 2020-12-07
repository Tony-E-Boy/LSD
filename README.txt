------------------------------------------------------------------------------------------------------------------------
When Is the Session Created?
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
    We can control exactly when our session gets created and how Spring Security will interact with it:
(Мы можем точно контролировать, когда будет создана наша сессия и как Spring Security будет взаимодействовать с ней:)

1. always – a session will always be created if one doesn't already exist
                   (сеанс всегда будет создан, если он еще не существует)

2. ifRequired – a session will be created only if required (default)
                  (сеанс будет создан только в случае необходимости)

3. never – the framework will never create a session itself but it will use one if it already exists
(фреймворк никогда не создаст сеанс сам по себе но он будет использовать его если он уже существует)

4. stateless – no session will be created or used by Spring Security
     (никакой сеанс не будет создан или использован Spring Security)
------------------------------------------------------------------------------------------------------------------------
<http create-session="ifRequired">...</http>
Java configuration:

@Override
protected void configure(HttpSecurity http) throws Exception {
    http.sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
}
------------------------------------------------------------------------------------------------------------------------
    Очень важно понимать, что эта конфигурация контролирует только то, что делает Spring Security, а не все приложение.
Spring Security может не создать сеанс, если мы проинструктируем его не делать этого, но наше приложение может!

По умолчанию Spring Security создает сеанс, когда он ему нужен – это “ifRequired".

Для более апатридного приложения опция “никогда " гарантирует, что Spring Security сама по себе не создаст никакого
сеанса; однако, если приложение создаст его, то Spring Security будет использовать его.

Наконец, самый строгий вариант создания сеанса - "без состояния" - является гарантией того, что приложение вообще
не создаст никакого сеанса.

Это было введено в Spring 3.1 и будет эффективно пропускать части цепочки фильтров безопасности Spring - в основном
связанные с сеансом части, такие как HttpSessionSecurityContextRepository, SessionManagementFilter, RequestCacheFilter.

Эти более строгие механизмы контроля имеют прямое отношение к тому, что файлы cookie не используются, и поэтому каждый
запрос должен быть повторно аутентифицирован. Эта архитектура без гражданства хорошо работает с API REST и их
ограничением безгражданства. Они также хорошо работают с механизмами аутентификации, такими как базовая(Basic) и
дайджест-аутентификация(Digest Authentication).

------------------------------------------------------------------------------------------------------------------------
Under the Hood
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
    Перед выполнением процесса аутентификации Spring Security запустит фильтр, отвечающий за хранение контекста
безопасностимежду запросами, - SecurityContextPersistenceFilter. Контекст будет храниться в соответствии со
стратегией-HttpSessionSecurityContextRepository по умолчанию-которая использует сеанс HTTP в качестве хранилища.

Для атрибута strict create-session= "stateless" эта стратегия будет заменена другой – NullSecurityContextRepository –
и никакой сеанс не будет создан или использован для сохранения контекста.

------------------------------------------------------------------------------------------------------------------------
 Concurrent Session Control
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

    Когда пользователь, который уже прошел проверку подлинности, пытается пройти проверку подлинности снова, приложение
может справиться с этим событием одним из нескольких способов. Он может либо аннулировать активный сеанс пользователя и
снова аутентифицировать пользователя с помощью нового сеанса, либо разрешить одновременное существование обоих сеансов.

The first step in enabling the concurrent session-control support is to add the following listener in the web.xml:
------------------------------------------------------------------------------------------------------------------------
<listener>
    <listener-class>
      org.springframework.security.web.session.HttpSessionEventPublisher
    </listener-class>
</listener>
------------------------------------------------------------------------------------------------------------------------

Or define it as a Bean – as follows:
------------------------------------------------------------------------------------------------------------------------
@Bean
public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
}
------------------------------------------------------------------------------------------------------------------------

Это необходимо для того, чтобы убедиться, что реестр сеансов Spring Security Session уведомляется об уничтожении сеанса.

Чтобы включить сценарий, допускающий несколько одновременных сеансов для одного и того же пользователя, в конфигурации
 XML следует использовать элемент управления сеансами:
 ------------------------------------------------------------------------------------------------------------------------
 <http ...>
     <session-management>
         <concurrency-control max-sessions="2" />
     </session-management>
 </http>
 Or, via Java configuration:

 @Override
 protected void configure(HttpSecurity http) throws Exception {
     http.sessionManagement().maximumSessions(2)
 }

------------------------------------------------------------------------------------------------------------------------
Session Timeout
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

Handling the Session Timeout

    После истечения времени ожидания сеанса, если пользователь отправляет запрос с истекшим идентификатором сеанса, он
будет перенаправлен на URL-адрес, настраиваемый через пространство имен:

<session-management>
    <concurrency-control expired-url="/sessionExpired.html" ... />
</session-management>
------------------------------------------------------------------------------------------------------------------------

    Аналогично, если пользователь отправляет запрос с идентификатором сеанса, который не истек, но полностью
недействителен, он также будет перенаправлен на настраиваемый URL-адрес:

<session-management invalid-session-url="/invalidSession.html">
    ...
</session-management>
------------------------------------------------------------------------------------------------------------------------

The corresponding Java configuration:

http.sessionManagement()
  .expiredUrl("/sessionExpired.html")
  .invalidSessionUrl("/invalidSession.html");
------------------------------------------------------------------------------------------------------------------------

Configure the Session Timeout with Spring Boot


We can easily configure the Session timeout value of the embedded server using properties:

server.servlet.session.timeout=15m
If we don't specify the duration unit, Spring will assume it's seconds.

In a nutshell, with this configuration, after 15 minutes of inactivity, the session will expire. The session after this
period of time is considered invalid.

If we configured our project to use Tomcat, we have to keep in mind that it only supports minute precision for session
timeout, with a minimum of one minute. This means that if we specify a timeout value of 170s for example, it will
result in a 2 minutes timeout.

Finally, it's important to mention that even though Spring Session supports a similar property for this purpose
(spring.session.timeout), if that's not specified then the autoconfiguration will fallback to the value of the property
we first mentioned.


------------------------------------------------------------------------------------------------------------------------
Prevent Using URL Parameters for Session Tracking
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

Exposing session information in the URL is a growing security risk (from place 7 in 2007 to place 2 in 2013 on the
OWASP Top 10 List).

Starting with Spring 3.0, the URL rewriting logic that would append the jsessionid to the URL can now be disabled by
setting the disable-url-rewriting=”true” in the <http> namespace.

Alternatively, starting with Servlet 3.0, the session tracking mechanism can also be configured in the web.xml:

<session-config>
     <tracking-mode>COOKIE</tracking-mode>
</session-config>
And programmatically:

servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
This chooses where to store the JSESSIONID – in the cookie or in a URL parameter.


------------------------------------------------------------------------------------------------------------------------
Session Fixation Protection With Spring Security
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

The framework offers protection against typical Session Fixation attacks by configuring what happens to an existing
session when the user tries to authenticate again:

<session-management session-fixation-protection="migrateSession"> ...
The corresponding Java configuration:

http.sessionManagement()
  .sessionFixation().migrateSession()
By default, Spring Security has this protection enabled (“migrateSession“) – on authentication a new HTTP Session is
created, the old one is invalidated and the attributes from the old session are copied over.

If this is not the desired behavior, two other options are available:

when “none” is set, the original session will not be invalidated
when “newSession” is set, a clean session will be created without any of the attributes from the old session being
copied over

------------------------------------------------------------------------------------------------------------------------
Secure Session Cookie
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

Next, we'll discuss how to secure our session cookie.

We can use the httpOnly and secure flags to secure our session cookie:

httpOnly: if true then browser script won't be able to access the cookie
secure: if true then the cookie will be sent only over HTTPS connection
We can set those flags for our session cookie in the web.xml:

<session-config>
    <session-timeout>1</session-timeout>
    <cookie-config>
        <http-only>true</http-only>
        <secure>true</secure>
    </cookie-config>
</session-config>
This configuration option is available since Java servlet 3. By default, http-only is true and secure is false.

Let's also have a look at the corresponding Java configuration:

public class MainWebAppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext sc) throws ServletException {
        // ...
        sc.getSessionCookieConfig().setHttpOnly(true);
        sc.getSessionCookieConfig().setSecure(true);
    }
}
If we're using Spring Boot, we can set these flags in our application.properties:

server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
Finally, we can also achieve this manually by using a Filter:

public class SessionFilter implements Filter {
    @Override
    public void doFilter(
      ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        Cookie[] allCookies = req.getCookies();
        if (allCookies != null) {
            Cookie session =
              Arrays.stream(allCookies).filter(x -> x.getName().equals("JSESSIONID"))
                    .findFirst().orElse(null);

            if (session != null) {
                session.setHttpOnly(true);
                session.setSecure(true);
                res.addCookie(session);
            }
        }
        chain.doFilter(req, res);
    }
}


------------------------------------------------------------------------------------------------------------------------
Working With the Session
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

Session Scoped Beans
------------------------------------------------------------------------------------------------------------------------

A bean can be defined with session scope simply by using the @Scope annotation on beans declared in the web-Context:

@Component
@Scope("session")
public class Foo { .. }
Or with XML:

<bean id="foo" scope="session"/>
Then, the bean can simply be injected into another bean:

@Autowired
private Foo theFoo;
And Spring will bind the new bean to the lifecycle of the HTTP Session.



Injecting the Raw Session into a Controller
------------------------------------------------------------------------------------------------------------------------
The raw HTTP Session can also be injected directly into a Controller method:

@RequestMapping(..)
public void fooMethod(HttpSession session) {
    session.setAttribute(Constants.FOO, new Foo());
    //...
    Foo foo = (Foo) session.getAttribute(Constants.FOO);
}

Obtaining the Raw Session
------------------------------------------------------------------------------------------------------------------------
The current HTTP Session can also be obtained programmatically via the raw Servlet API:

ServletRequestAttributes attr = (ServletRequestAttributes)
    RequestContextHolder.currentRequestAttributes();
HttpSession session= attr.getRequest().getSession(true); // true == allow create


