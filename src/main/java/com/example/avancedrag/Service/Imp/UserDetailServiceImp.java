package com.example.avancedrag.Service.Imp;

import com.example.avancedrag.Service.Dto.UtilisateurDto;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImp implements UserDetailsService {

    public UserDetailServiceImp(UtilisateurServiceeImp utilisateurServiceeImp) {
        this.utilisateurServiceeImp = utilisateurServiceeImp;
    }

    UtilisateurServiceeImp utilisateurServiceeImp;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UtilisateurDto utilisateurDto = utilisateurServiceeImp.loadUserByUsername(username);
        if(utilisateurDto==null) throw new UsernameNotFoundException("pas D'utilisateur trouver");
        String authorities = String.valueOf(utilisateurDto.getRole().getRole());
        UserDetails userDetails = User
                .withUsername(utilisateurDto.getUsername())
                .password(utilisateurDto.getPassword())
                .authorities(authorities)
                .build();
        return userDetails;
    }
}
