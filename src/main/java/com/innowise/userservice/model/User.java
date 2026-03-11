package com.innowise.userservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_name_surname", columnList = "name, surname")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends Auditable {

  @Id
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 100)
  private String surname;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "birth_date",columnDefinition = "DATE")
  private LocalDate birthDate;

  @Builder.Default
  @Column(nullable = false)
  private boolean active = true;

  @OneToMany(mappedBy = "user",
              cascade = CascadeType.ALL,
          orphanRemoval = true)
  private List<PaymentCard> paymentCards;

}
