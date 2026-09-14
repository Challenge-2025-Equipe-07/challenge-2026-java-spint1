package br.com.fiap.veti.controller;

import br.com.fiap.veti.dto.request.LoginRequest;
import br.com.fiap.veti.dto.request.RegisterRequest;
import br.com.fiap.veti.dto.response.LoginResponse;
import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.mapper.VeterinarioMapper;
import br.com.fiap.veti.repository.VeterinarioRepository;
import br.com.fiap.veti.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final VeterinarioRepository veterinarioRepository;
    private final VeterinarioMapper veterinarioMapper;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public ModelAndView loginPage() {
        return new ModelAndView("login");
    }


    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        var token = autenticarEGerarToken(loginRequest.email(), loginRequest.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }


    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView loginForm(@RequestParam String email,
                                  @RequestParam String password,
                                  HttpServletResponse response) {
        var token = autenticarEGerarToken(email, password);

        Cookie cookie = new Cookie("veti_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return new RedirectView("/veti/consultas/painel");
    }

    private String autenticarEGerarToken(String email, String password) {
        var usuarioSenha = new UsernamePasswordAuthenticationToken(email, password);
        var auth = this.authenticationManager.authenticate(usuarioSenha);
        return tokenService.generateToken((VeterinarioEntity) auth.getPrincipal());
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest registerDTO) {
        if (veterinarioRepository.findByEmail(registerDTO.email()) != null) {
            return ResponseEntity.badRequest().build();
        }
        VeterinarioEntity veterinario = veterinarioMapper.toEntity(registerDTO);
        veterinario.setPassword(passwordEncoder.encode(registerDTO.password()));
        veterinarioRepository.save(veterinario);
        return ResponseEntity.ok().build();
    }
}