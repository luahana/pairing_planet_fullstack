package com.pairingplanet.pairing_planet.domain.entity.post;

import com.pairingplanet.pairing_planet.domain.entity.common.BaseEntity;
import com.pairingplanet.pairing_planet.domain.entity.hashtag.Hashtag;
import com.pairingplanet.pairing_planet.domain.entity.image.Image;
import com.pairingplanet.pairing_planet.domain.entity.pairing.PairingMap;
import com.pairingplanet.pairing_planet.domain.entity.user.User;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Generated;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "posts")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pairing_id", nullable = false)
    private PairingMap pairing;

    @Column(nullable = false)
    private String locale;

    @Column(name = "title")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @Column(name = "genius_count")
    @ColumnDefault("0")
    private int geniusCount;

    @Column(name = "daring_count")
    @ColumnDefault("0")
    private int daringCount;

    @Column(name = "picky_count")
    @ColumnDefault("0")
    private int pickyCount;

    @Column(name = "saved_count")
    @ColumnDefault("0")
    private int savedCount;

    @Column(name = "comment_count")
    @ColumnDefault("0")
    private int commentCount;

    @Column(name = "popularity_score", insertable = false, updatable = false)
    @Generated(GenerationTime.ALWAYS)
    private Double popularityScore;

    @Column(name = "controversy_score", insertable = false, updatable = false)
    @Generated(GenerationTime.ALWAYS)
    private Double controversyScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "comments_enabled")
    private boolean commentsEnabled;

    // [수정된 부분] 초기값(= false)을 넣어주어야 합니다.
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "is_private", nullable = false)
    @Builder.Default
    private boolean isPrivate = false;

    @ManyToMany
    @JoinTable(
            name = "post_hashtag_map",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    @Builder.Default
    @BatchSize(size = 100)
    private Set<Hashtag> hashtags = new LinkedHashSet<>();

    public void setHashtags(Set<Hashtag> hashtags) {
        this.hashtags = hashtags;
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}