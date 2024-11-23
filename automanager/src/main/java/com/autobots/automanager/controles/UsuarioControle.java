package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.jwt.ProvedorJwt;
import com.autobots.automanager.modelos.adicionadoresLink.AdicionadorLinkUsuario;
import com.autobots.automanager.modelos.atualizadores.UsuarioAtualizador;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
public class UsuarioControle {
    @Autowired
    private RepositorioUsuario repositorio;

    @Autowired
    private AdicionadorLinkUsuario adicionadorLink;

    @Autowired
    private UsuarioAtualizador atualizador;

    @Autowired
    private AuthenticationManager authenticationManager;


    @Autowired
    private ProvedorJwt provedorJwt;

    @GetMapping("/usuario/{id}")
    public ResponseEntity<?> obterUsuario(@PathVariable Long id) {
        try {
            if (!repositorio.findById(id).isPresent())
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            Usuario usuario = repositorio.findById(id).get();
            adicionadorLink.adicionarLink(usuario);

            return new ResponseEntity<>(usuario, HttpStatus.FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> obterUsuarios() {
        try {
            if (repositorio.findAll().isEmpty())
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            List<Usuario> usuarios = repositorio.findAll();
            adicionadorLink.adicionarLink(usuarios);

            return new ResponseEntity<>(usuarios, HttpStatus.FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/usuario/cadastro")
    public ResponseEntity<?> cadastrarUsuario(@RequestBody Usuario usuario) {
        try {
            repositorio.save(usuario);

            adicionadorLink.adicionarLink(usuario);

            return new ResponseEntity<>(usuario, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> criarTokenDeAutenticacao(@RequestBody Credencial credenciais) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credenciais.getNomeUsuario(), credenciais.getSenha()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Continuação do método, como geração do token JWT e retorno da resposta
            String token = provedorJwt.proverJwt(credenciais.getNomeUsuario());
        return new ResponseEntity<>(token, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/usuario/atualizar")
    public ResponseEntity<?> atualizarUsuario(@RequestBody Usuario atualizacao) {
        try {
            if (!repositorio.findById(atualizacao.getId()).isPresent())
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            Usuario usuario = repositorio.findById(atualizacao.getId()).get();
            atualizador.atualizar(usuario, atualizacao);
            repositorio.save(usuario);

            adicionadorLink.adicionarLink(usuario);

            return new ResponseEntity<>(usuario, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/usuario/excluir")
    public ResponseEntity<?> deletarUsuario(@RequestBody Usuario exclusao) {
        try {
            if (!repositorio.findById(exclusao.getId()).isPresent())
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            Usuario usuario = repositorio.findById(exclusao.getId()).get();
            repositorio.delete(usuario);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
