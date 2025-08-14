package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "name_pool")
public class NamePool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column(name = "base_name", nullable = false, length = 50)
    private String baseName;

    @Column(name = "next_seq", nullable = false)
    private Integer nextSeq;

    public String generate() {
        String nickname = baseName + nextSeq;
        this.nextSeq++;
        return nickname;
    }
}