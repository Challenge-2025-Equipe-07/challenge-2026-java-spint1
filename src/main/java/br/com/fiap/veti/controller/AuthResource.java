package br.com.fiap.veti.controller;

import br.com.fiap.veti.dto.request.LoginRequest;
import br.com.fiap.veti.dto.request.RegisterRequest;
import br.com.fiap.veti.dto.response.LoginResponse;
import br.com.fiap.veti.dto.response.VeterinarioResponse;
import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.mapper.VeterinarioMapper;
import br.com.fiap.veti.repository.VeterinarioRepository;
import br.com.fiap.veti.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthResource {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    VeterinarioRepository veterinarioRepository;

    @Autowired
    VeterinarioMapper veterinarioMapper;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginRequest loginRequest) {
        var usuarioSenha = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
        var auth = this.authenticationManager.authenticate(usuarioSenha);
        var token = tokenService.generateToken((VeterinarioEntity) auth.getPrincipal());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterRequest registerDTO) {
        if (veterinarioRepository.findByEmail(registerDTO.email()) != null) {
            return ResponseEntity.badRequest().build();
        }
        String encryptedPassword = new
                BCryptPasswordEncoder().encode(registerDTO.password());
        RegisterRequest novoUsuario = new RegisterRequest(registerDTO.email(), encryptedPassword,
                registerDTO.role());
                   veterinarioRepository.save(veterinarioMapper.toEntity(novoUsuario));
        return ResponseEntity.ok().build();

    }
}