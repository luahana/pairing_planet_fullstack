package com.pairingplanet.pairing_planet.domain.entity.comment;

import com.pairingplanet.pairing_planet.domain.entity.common.BaseEntity;
import com.pairingplanet.pairing_planet.domain.enums.VerdictType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {
    private Long postId;
    private Long userId;

    // [수정] Long parentId -> Comment parent (자기 참조 연관관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private VerdictType initialVerdict;

    @Setter
    @Enumerated(EnumType.STRING)
    private VerdictType currentVerdict;

    @Builder.Default
    private int likeCount = 0;

    @Builder.Default
    private boolean isDeleted = false;

    // 비즈니스 메서드
    public void increaseLike() { this.likeCount++; }
    public void decreaseLike() { this.likeCount--; }
    public void syncVerdict(VerdictType newVerdict) { this.currentVerdict = newVerdict; }

    // 삭제 처리 메서드 추가
    public void softDelete() { this.isDeleted = true; }
}