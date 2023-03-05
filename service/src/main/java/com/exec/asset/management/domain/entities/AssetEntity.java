package com.exec.asset.management.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Data
@Builder
@Entity
@Table(name = "assets")
public class AssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Type(type = "uuid-char")
    @Column
    private UUID id;

    @Column
    private Boolean promoted;

    @Column
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private AssetEntity parentEntity;
}
