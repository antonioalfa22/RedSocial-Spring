/**
 * 
 */
package com.social.servicios;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.social.entidades.Amistad;
import com.social.entidades.Usuario;
import com.social.repositorios.PeticionAmistadRepository;
import com.social.repositorios.UsuariosRepository;

/**
 * <h1>UsuarioService</h1>
 * 
 * Servicio que se encarga de realizar las operaciones con usuarios
 * 
 * @author Antonio Paya Gonzalez
 * @author Pablo Diaz Rancaño
 *
 */
@Service
public class UsuarioService {

	@Autowired
	private UsuariosRepository usuariosRepository;
	
	@Autowired
	private PeticionAmistadRepository amistadRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public List<Usuario> getUsuarios() {
		List<Usuario> usuarios = new ArrayList<Usuario>();
		usuariosRepository.findAll().forEach(usuarios::add);
		return usuarios;
	}

	public Usuario getUsuario(Long id) {
		return usuariosRepository.findOne(id);
	}

	public void addUsuario(Usuario usuario) {
		usuario.setPassword(bCryptPasswordEncoder.encode(usuario.getPassword()));
		usuariosRepository.save(usuario);
		log.trace("Añadido el usuario: "+usuario);
	}

	public void deleteUsuario(Long id) {
		usuariosRepository.delete(id);
		log.trace("Borrado el usuario: "+this.getUsuario(id));
	}
	
	public Usuario getUsuarioActivo() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = auth.getName();
		Usuario activeUser = getUserByUsername(username);
		return activeUser;
	}

	public Usuario getUserByUsername(String username) {
		return usuariosRepository.findByUsername(username);
	}

	public Object getUserByEmail(String email) {
		return usuariosRepository.findByEmail(email);
	}

	public Page<Usuario> getUsuarios(Pageable pageable){
		Page<Usuario> usuarios = usuariosRepository.findAll(pageable);
		return usuarios;
	}
	
	public Page<Usuario> getUsuariosAmigos(Pageable pageable,Usuario u){
		Page<Usuario> usuarios;
		List<Usuario> amigos = u.getAmigos().stream().collect(Collectors.toList());
		usuarios = new PageImpl<Usuario>(amigos);
		return usuarios;
	}
	
	public Page<Usuario> buscarUsuariosPorNombreOEmail(Pageable pageable, String searchText)
	{
		Page<Usuario> usuarios = new PageImpl<Usuario>(new LinkedList<Usuario>());
		
		searchText = "%"+searchText+"%";
		
		usuarios = usuariosRepository.buscarPorNombreOEmail(pageable, searchText);
	
		return usuarios;
	}
	
	public void addPeticionAmistad(Usuario u1, Usuario u2)
	{
		amistadRepository.save( new Amistad( u1.getId(), u2.getId()) );
	}
	
	public void aceptarPeticionAmistad(Usuario u1, Usuario u2)
	{
		amistadRepository.delete(u1.getId(), u2.getId());
		
		u1.addAmigo( u2 );
		u2.addAmigo( u1 );
	}
	
	public void rechazarPeticionAmistad(Usuario u1, Usuario u2)
	{
		amistadRepository.delete(u1.getId(), u2.getId());
	}
	
	public List<Long> getPeticionesEnviadas(Usuario usuario)
	{
		return usuariosRepository.findPeticionesEnviadas( usuario.getId() );
	}
}
