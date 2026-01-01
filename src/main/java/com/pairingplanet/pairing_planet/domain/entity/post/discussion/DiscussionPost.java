package com.pairingplanet.pairing_planet.domain.entity.post.discussion;

import com.pairingplanet.pairing_planet.domain.entity.post.Post;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "discussion_posts") // SQL 테이블 이름과 일치시킵니다.
@PrimaryKeyJoinColumn(name = "post_id")
@DiscriminatorValue("DISCUSSION")
@Getter
@Setter
@SuperBuilder // [핵심] 부모의 빌더를 확장하기 위해 필수
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 필수
@AllArgsConstructor
public class DiscussionPost extends Post {

    @Column(name = "verdict_enabled")
    private boolean verdictEnabled;

}