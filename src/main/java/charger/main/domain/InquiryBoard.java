package charger.main.domain;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.Generated;

import charger.main.domain.embeded.FavoriteStoreId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
		name = "inquiry_board")
public class InquiryBoard {
	@Id@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String content;
	private String title;
	@ManyToOne
	@JoinColumn(name="username")
	private Member member;
	@Column(name="created_at")
	private LocalDateTime createdAt;
	@Column(name="updated_at")
	private LocalDateTime updatedAt;
}
