package com.exec.asset.management.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "assets")
public class AssetEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @Type(type = "uuid-char")
    @Column
    private UUID id;

    @Column
    private Boolean promoted;

    @Type(type = "uuid-char")
    @Column
    private UUID parentId;
}
