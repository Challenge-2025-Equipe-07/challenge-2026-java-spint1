package br.com.fiap.veti.controller;

import br.com.fiap.veti.config.AuthCookies;
import br.com.fiap.veti.dto.request.LoginRequest;
import br.com.fiap.veti.dto.request.RegisterRequest;
import br.com.fiap.veti.dto.response.LoginResponse;
import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.enms.Role;
import br.com.fiap.veti.service.TokenService;
import br.com.fiap.veti.service.VeterinarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final VeterinarioService veterinarioService;
    private final TokenService tokenService;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

    @GetMapping("/login")
    public ModelAndView loginPage(@RequestParam(required = false) String erro,
                                  @RequestParam(required = false) String saiu) {
        var mv = new ModelAndView("login");
        mv.addObject("googleEnabled", clientRegistrationRepository.getIfAvailable() != null);
        mv.addObject("erro", erro != null);
        mv.addObject("saiu", saiu != null);
        return mv;
    }


    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        var token = autenticarEGerarToken(loginRequest.email(), loginRequest.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }


    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView loginForm(@RequestParam String email,
                                  @RequestParam String password,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        try {
            var token = autenticarEGerarToken(email, password);
            response.addHeader(HttpHeaders.SET_COOKIE, AuthCookies.create(token, request.isSecure()));
            return new RedirectView("/", true);
        } catch (AuthenticationException e) {
            log.warn("Falha de login pelo formulario");
            return new RedirectView("/auth/login?erro", true);
        }
    }

    @PostMapping("/logout")
    public RedirectView logout(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, AuthCookies.clear(request.isSecure()));
        return new RedirectView("/auth/login?saiu", true);
    }

    private String autenticarEGerarToken(String email, String password) {
        var usuarioSenha = new UsernamePasswordAuthenticationToken(email, password);
        var auth = this.authenticationManager.authenticate(usuarioSenha);
        return tokenService.generateToken((VeterinarioEntity) auth.getPrincipal());
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest registerDTO,
                                         @AuthenticationPrincipal VeterinarioEntity usuarioLogado) {
        // Auto-cadastro público sempre cria USER; só um ADMIN autenticado pode criar outro ADMIN
        boolean chamadorAdmin = usuarioLogado != null && Role.ADMIN.name().equals(usuarioLogado.getRole());
        Role role = chamadorAdmin && registerDTO.role() != null ? registerDTO.role() : Role.USER;
        veterinarioService.cadastrar(registerDTO, role);
        return ResponseEntity.ok().build();
    }
}
