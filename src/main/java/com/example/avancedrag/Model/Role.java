package com.example.avancedrag.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "role")
public class Role {

//    @Id
//    @Column(name = "nom")
//    @Enumerated(EnumType.STRING)
//    private Roles role;


    @Id
    @Column(name = "nom")
    private String role;

    @OneToMany(mappedBy = "role")
    @JsonIgnore
    private List<Utilisateur> utilisateur;




}
