package charger.main.domain;

import java.time.LocalDateTime;
import java.util.List;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
	@Id
	private String username;
	@Column(nullable = false)
	private String nickname;
	@Column(nullable = false)
	private String password;
	@Column(nullable = false)
	private String phoneNumber;
	@Column(nullable = false)
	private String email;
	private String sex;
	@Enumerated(EnumType.STRING)
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "member_roles",joinColumns = @JoinColumn(name= "username"))
	@Column(columnDefinition = "varchar(32) default 'ROLE_MEMBER'",nullable = false)
	private List<Role> role;
	@Column(nullable = false)
	private boolean enabled;
	private String address;
	@Column(nullable = false,name = "create_at")
	private LocalDateTime createAt;
}
