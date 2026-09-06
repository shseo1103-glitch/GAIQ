package kr.co.gaiq.certificate.repository;

import kr.co.gaiq.certificate.entity.CertificateCoa;
import org.springframework.data.jpa.repository.JpaRepository;

/** 2단계 예정 — 1단계는 스키마/엔티티/리포지토리만 존재, 서비스/컨트롤러 미구현. */
public interface CertificateCoaRepository extends JpaRepository<CertificateCoa, Long> {
}
