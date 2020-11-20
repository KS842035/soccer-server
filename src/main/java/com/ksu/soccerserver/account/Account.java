package com.ksu.soccerserver.account;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ksu.soccerserver.application.ApplicationAccount;
import com.ksu.soccerserver.invitation.InvitationAccount;
import com.ksu.soccerserver.team.Team;

@Builder
@Entity @Table
@Getter
@NoArgsConstructor @AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Email 길이=100, UNIQUE, Not NULL
    @Column(length = 100, nullable = false, unique = true)
    private String email;

    //Password 길이=400, UNIQUE, Not NULL
    @JsonIgnore
    @Column(length = 400, nullable = false)
    private String password;

    @Column
    private String name;

    @Column
    private String phoneNum;

    @Column
    private String birth;

    @Column
    private String gender;


    @OneToMany(mappedBy = "account")
    private final Set<ApplicationAccount> apply = new HashSet<>();

    @ManyToOne
    private Team team;

    @OneToOne
    Team leadingTeam;

    @OneToMany(mappedBy = "account")
    private final Set<InvitationAccount> invitationAccount = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    public void updateMyInfo(String name) { this.name = name; }

    public void joinTeam(Team team) { this.team = team; }

    public void changePW(String password) { this.password = password;}

    public void setLeadingTeam(Team team) { this.leadingTeam = team; }

    public void setTeam(Team team) { this.team = team; }

}
