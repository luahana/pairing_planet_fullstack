package com.pairingplanet.pairing_planet.domain.entity.post.daily_log;

import com.pairingplanet.pairing_planet.domain.entity.post.Post;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "daily_posts") // [필수] SQL의 'daily_posts'와 일치시켜야 함
@PrimaryKeyJoinColumn(name = "post_id")
@DiscriminatorValue("DAILY")
@Getter
@Setter
@SuperBuilder
public class DailyPost extends Post {
}