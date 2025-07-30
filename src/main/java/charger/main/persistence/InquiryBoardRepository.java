package charger.main.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import charger.main.domain.InquiryBoard;

public interface InquiryBoardRepository extends JpaRepository<InquiryBoard, Long>{
}
