package fr.insee.pearljam.api.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.insee.pearljam.api.domain.Campaign;
import fr.insee.pearljam.api.dto.campaign.CampaignDto;
import fr.insee.pearljam.api.dto.interviewer.InterviewerDto;
import fr.insee.pearljam.api.dto.message.VerifyNameResponseDto;

/**
 * CampaignRepository is the repository using to access to Campaign table in DB
 * 
 * @author scorcaud
 * 
 */
public interface CampaignRepository extends JpaRepository<Campaign, String> {

	Optional<Campaign> findByIdIgnoreCase(String id);

	@Query(value = "SELECT id FROM campaign ", nativeQuery = true)
	List<String> findAllIds();

	@Query(value = "SELECT DISTINCT(campaign_id) FROM visibility WHERE " 
			+ "organization_unit_id IN (:OuIds) "
			+ "AND management_start_date <= :date " 
			+ "AND collection_start_date <= :date " 
			+ "AND collection_end_date > :date  ", nativeQuery = true)
	List<String> findAllIdsVisible(@Param("OuIds") List<String> ouIds, @Param("date") Long date);

	@Query(value = "SELECT camp.id " 
			+ "FROM campaign camp " 
			+ "INNER JOIN visibility vi ON vi.campaign_id = camp.id "
			+ "INNER JOIN organization_unit ou ON ou.id = vi.organization_unit_id "
			+ "WHERE ou.id ILIKE ?1", nativeQuery = true)
	List<String> findIdsByOuId(String ouId);

	@Query(value = "SELECT new fr.insee.pearljam.api.dto.campaign.CampaignDto(camp.id, camp.label) " 
			+ "FROM Campaign camp " 
			+ "WHERE camp.id=?1")
	CampaignDto findDtoById(String id);

	@Query(value = "SELECT 1 " 
			+ "FROM preference pref " 
			+ "WHERE pref.id_user ILIKE ?1 "
			+ "AND pref.id_campaign = ?2", nativeQuery = true)
	List<Integer> checkCampaignPreferences(String userId, String campaignId);

	@Query("SELECT "
			+ "new fr.insee.pearljam.api.dto.interviewer.InterviewerDto(interv.id, interv.firstName, interv.lastName, COUNT(su.interviewer)) "
			+ "FROM SurveyUnit su " 
			+ "INNER JOIN Interviewer interv ON su.interviewer.id = interv.id "
			+ "WHERE su.campaign.id=?1 " 
			+ "AND (interv.organizationUnit.id=?2 OR ?2='GUEST') " 
			+ "GROUP BY interv.id")
	List<InterviewerDto> findInterviewersDtoByCampaignIdAndOrganisationUnitId(String id, String organizationUnitId);

	@Query(value = "SELECT " 
			+ "SUM(CASE WHEN type='NVM' THEN 1 ELSE 0 END) AS nvmCount, "
			+ "SUM(CASE WHEN type='NNS' THEN 1 ELSE 0 END) AS nnsCount, "
			+ "SUM(CASE WHEN type='ANV' THEN 1 ELSE 0 END) AS anvCount, "
			+ "SUM(CASE WHEN type='VIN' THEN 1 ELSE 0 END) AS vinCount, "
			+ "SUM(CASE WHEN type='VIC' THEN 1 ELSE 0 END) AS vicCount, "
			+ "SUM(CASE WHEN type='PRC' THEN 1 ELSE 0 END) AS prcCount, "
			+ "SUM(CASE WHEN type='AOC' THEN 1 ELSE 0 END) AS aocCount, "
			+ "SUM(CASE WHEN type='APS' THEN 1 ELSE 0 END) AS apsCount, "
			+ "SUM(CASE WHEN type='INS' THEN 1 ELSE 0 END) AS insCount, "
			+ "SUM(CASE WHEN type='WFT' THEN 1 ELSE 0 END) AS wftCount, "
			+ "SUM(CASE WHEN type='WFS' THEN 1 ELSE 0 END) AS wfsCount, "
			+ "SUM(CASE WHEN type='TBR' THEN 1 ELSE 0 END) AS tbrCount, "
			+ "SUM(CASE WHEN type='FIN' THEN 1 ELSE 0 END) AS finCount, "
			+ "SUM(CASE WHEN type='QNA' THEN 1 ELSE 0 END) AS qnaCount, "
			+ "SUM(CASE WHEN type='QNA' AND EXISTS (SELECT 1 FROM state s where s.type ='FIN' AND s.survey_unit_id = t.survey_unit_id) THEN 1 ELSE 0 END) AS qnaFinCount, " 
			+ "SUM(CASE WHEN type='NVA' THEN 1 ELSE 0 END) AS nvaCount, " 
			+ "COUNT(1) AS total " 
			+ "FROM ( "
			+ "SELECT DISTINCT(survey_unit_id), type, date FROM state WHERE (survey_unit_id, date) IN ("
			+ "SELECT survey_unit_id, MAX(date) FROM state WHERE survey_unit_id IN (" 
			+ "SELECT id FROM survey_unit "
			+ "WHERE campaign_id=?1 " 
			+ "AND interviewer_id=?2 ) " 
			+ "AND (date<=?3 OR ?3<0) GROUP BY survey_unit_id) "
			+ ") as t", nativeQuery = true)
	Map<String,BigInteger> getStateCount(String campaignId, String interviewerId, Long date);

	@Query(value = "SELECT " 
			+ "SUM(CASE WHEN type='NVM' THEN 1 ELSE 0 END) AS nvmCount, "
			+ "SUM(CASE WHEN type='NNS' THEN 1 ELSE 0 END) AS nnsCount, "
			+ "SUM(CASE WHEN type='ANV' THEN 1 ELSE 0 END) AS anvCount, "
			+ "SUM(CASE WHEN type='VIN' THEN 1 ELSE 0 END) AS vinCount, "
			+ "SUM(CASE WHEN type='VIC' THEN 1 ELSE 0 END) AS vicCount, "
			+ "SUM(CASE WHEN type='PRC' THEN 1 ELSE 0 END) AS prcCount, "
			+ "SUM(CASE WHEN type='AOC' THEN 1 ELSE 0 END) AS aocCount, "
			+ "SUM(CASE WHEN type='APS' THEN 1 ELSE 0 END) AS apsCount, "
			+ "SUM(CASE WHEN type='INS' THEN 1 ELSE 0 END) AS insCount, "
			+ "SUM(CASE WHEN type='WFT' THEN 1 ELSE 0 END) AS wftCount, "
			+ "SUM(CASE WHEN type='WFS' THEN 1 ELSE 0 END) AS wfsCount, "
			+ "SUM(CASE WHEN type='TBR' THEN 1 ELSE 0 END) AS tbrCount, "
			+ "SUM(CASE WHEN type='FIN' THEN 1 ELSE 0 END) AS finCount, "
			+ "SUM(CASE WHEN type='QNA' THEN 1 ELSE 0 END) AS qnaCount, "
			+ "SUM(CASE WHEN type='QNA' AND EXISTS (SELECT 1 FROM state s where s.type ='FIN' AND s.survey_unit_id = t.survey_unit_id) THEN 1 ELSE 0 END) AS qnaFinCount, " 
			+ "SUM(CASE WHEN type='NVA' THEN 1 ELSE 0 END) AS nvaCount, " 
			+ "COUNT(1) AS total " 
			+ "FROM ( "
			+ "SELECT DISTINCT(survey_unit_id), type, date FROM state WHERE (survey_unit_id, date) IN ("
			+ "SELECT survey_unit_id, MAX(date) FROM state WHERE survey_unit_id IN (" 
			+ "SELECT id FROM survey_unit "
			+ "WHERE campaign_id=:campaignId " 
			+ "AND interviewer_id IN ("
			+ "SELECT id FROM interviewer WHERE organization_unit_id IN (:ouIds)) " 
			+ ") "
			+ "AND (date<=:date OR :date<0) GROUP BY survey_unit_id) " + ") as t", nativeQuery = true)
	Map<String,BigInteger> getStateCountSumByCampaign(@Param("campaignId") String campaignId,
			@Param("ouIds") List<String> ouIds, @Param("date") Long date);

	@Query(value = "SELECT "
			+ "SUM(CASE WHEN type='NVM' THEN 1 ELSE 0 END) AS nvmCount, "
			+ "SUM(CASE WHEN type='NNS' THEN 1 ELSE 0 END) AS nnsCount, "
			+ "SUM(CASE WHEN type='ANV' THEN 1 ELSE 0 END) AS anvCount, "
			+ "SUM(CASE WHEN type='VIN' THEN 1 ELSE 0 END) AS vinCount, "
			+ "SUM(CASE WHEN type='VIC' THEN 1 ELSE 0 END) AS vicCount, "
			+ "SUM(CASE WHEN type='PRC' THEN 1 ELSE 0 END) AS prcCount, "
			+ "SUM(CASE WHEN type='AOC' THEN 1 ELSE 0 END) AS aocCount, "
			+ "SUM(CASE WHEN type='APS' THEN 1 ELSE 0 END) AS apsCount, "
			+ "SUM(CASE WHEN type='INS' THEN 1 ELSE 0 END) AS insCount, "
			+ "SUM(CASE WHEN type='WFT' THEN 1 ELSE 0 END) AS wftCount, "
			+ "SUM(CASE WHEN type='WFS' THEN 1 ELSE 0 END) AS wfsCount, "
			+ "SUM(CASE WHEN type='TBR' THEN 1 ELSE 0 END) AS tbrCount, "
			+ "SUM(CASE WHEN type='FIN' THEN 1 ELSE 0 END) AS finCount, "
			+ "SUM(CASE WHEN type='QNA' THEN 1 ELSE 0 END) AS qnaCount, "
			+ "SUM(CASE WHEN type='QNA' AND EXISTS (SELECT 1 FROM state s where s.type ='FIN' AND s.survey_unit_id = t.survey_unit_id) THEN 1 ELSE 0 END) AS qnaFinCount, " 
			+ "SUM(CASE WHEN type='NVA' THEN 1 ELSE 0 END) AS nvaCount, "
			+ "COUNT(1) AS total " 
			+ "FROM ( "
			+ "SELECT DISTINCT(survey_unit_id), type, date FROM state WHERE (survey_unit_id, date) IN ("
			+ "SELECT survey_unit_id, MAX(date) FROM state WHERE survey_unit_id IN (" 
			+ "SELECT id FROM survey_unit "
			+ "WHERE campaign_id IN (:campaignIds) " 
			+ "AND interviewer_id=:interviewerId ) "
			+ "AND (date<=:date OR :date<0) GROUP BY survey_unit_id) " 
			+ ") as t", nativeQuery = true)
	Map<String,BigInteger> getStateCountSumByInterviewer(@Param("campaignIds") List<String> campaignId,
			@Param("interviewerId") String interviewerId, @Param("date") Long date);

	@Query(value = "SELECT " 
			+ "SUM(CASE WHEN type='NVM' THEN 1 ELSE 0 END) AS nvmCount, "
			+ "SUM(CASE WHEN type='NNS' THEN 1 ELSE 0 END) AS nnsCount, "
			+ "SUM(CASE WHEN type='ANV' THEN 1 ELSE 0 END) AS anvCount, "
			+ "SUM(CASE WHEN type='VIN' THEN 1 ELSE 0 END) AS vinCount, "
			+ "SUM(CASE WHEN type='VIC' THEN 1 ELSE 0 END) AS vicCount, "
			+ "SUM(CASE WHEN type='PRC' THEN 1 ELSE 0 END) AS prcCount, "
			+ "SUM(CASE WHEN type='AOC' THEN 1 ELSE 0 END) AS aocCount, "
			+ "SUM(CASE WHEN type='APS' THEN 1 ELSE 0 END) AS apsCount, "
			+ "SUM(CASE WHEN type='INS' THEN 1 ELSE 0 END) AS insCount, "
			+ "SUM(CASE WHEN type='WFT' THEN 1 ELSE 0 END) AS wftCount, "
			+ "SUM(CASE WHEN type='WFS' THEN 1 ELSE 0 END) AS wfsCount, "
			+ "SUM(CASE WHEN type='TBR' THEN 1 ELSE 0 END) AS tbrCount, "
			+ "SUM(CASE WHEN type='FIN' THEN 1 ELSE 0 END) AS finCount, "
			+ "SUM(CASE WHEN type='QNA' THEN 1 ELSE 0 END) AS qnaCount, "
			+ "SUM(CASE WHEN type='QNA' AND EXISTS (SELECT 1 FROM state s where s.type ='FIN' AND s.survey_unit_id = t.survey_unit_id) THEN 1 ELSE 0 END) AS qnaFinCount, " 
			+ "SUM(CASE WHEN type='NVA' THEN 1 ELSE 0 END) AS nvaCount, "
			+ "COUNT(1) AS total " 
			+ "FROM ( "
			+ "SELECT DISTINCT(survey_unit_id), type, date FROM state WHERE (survey_unit_id, date) IN ("
			+ "SELECT survey_unit_id, MAX(date) FROM state WHERE survey_unit_id IN (" 
			+ "SELECT id FROM survey_unit "
			+ "WHERE campaign_id=?1 "
			+ "AND interviewer_id IN (SELECT int.id FROM interviewer int WHERE int.organization_unit_id =?2 )) "
			+ "AND (date<=?3 OR ?3<0) GROUP BY survey_unit_id) " 
			+ ") as t", nativeQuery = true)
	Map<String,BigInteger> getStateCountByCampaignAndOU(String campaignId, String organizationalUnitId, Long date);

	@Query(value = "SELECT " 
			+ "SUM(CASE WHEN type='NVM' THEN 1 ELSE 0 END) AS nvmCount, "
			+ "SUM(CASE WHEN type='NNS' THEN 1 ELSE 0 END) AS nnsCount, "
			+ "SUM(CASE WHEN type='ANV' THEN 1 ELSE 0 END) AS anvCount, "
			+ "SUM(CASE WHEN type='VIN' THEN 1 ELSE 0 END) AS vinCount, "
			+ "SUM(CASE WHEN type='VIC' THEN 1 ELSE 0 END) AS vicCount, "
			+ "SUM(CASE WHEN type='PRC' THEN 1 ELSE 0 END) AS prcCount, "
			+ "SUM(CASE WHEN type='AOC' THEN 1 ELSE 0 END) AS aocCount, "
			+ "SUM(CASE WHEN type='APS' THEN 1 ELSE 0 END) AS apsCount, "
			+ "SUM(CASE WHEN type='INS' THEN 1 ELSE 0 END) AS insCount, "
			+ "SUM(CASE WHEN type='WFT' THEN 1 ELSE 0 END) AS wftCount, "
			+ "SUM(CASE WHEN type='WFS' THEN 1 ELSE 0 END) AS wfsCount, "
			+ "SUM(CASE WHEN type='TBR' THEN 1 ELSE 0 END) AS tbrCount, "
			+ "SUM(CASE WHEN type='FIN' THEN 1 ELSE 0 END) AS finCount, "
			+ "SUM(CASE WHEN type='QNA' THEN 1 ELSE 0 END) AS qnaCount, "
			+ "SUM(CASE WHEN type='QNA' AND EXISTS (SELECT 1 FROM state s where s.type ='FIN' AND s.survey_unit_id = t.survey_unit_id) THEN 1 ELSE 0 END) AS qnaFinCount, " 
			+ "SUM(CASE WHEN type='NVA' THEN 1 ELSE 0 END) AS nvaCount, " 
			+ "COUNT(1) AS total " 
			+ "FROM ( "
			+ "SELECT DISTINCT(survey_unit_id), type, date FROM state WHERE (survey_unit_id, date) IN ("
			+ "SELECT survey_unit_id, MAX(date) FROM state WHERE survey_unit_id IN (" 
			+ "SELECT id FROM survey_unit "
			+ "WHERE campaign_id=?1) " 
			+ "AND (date<=?2 OR ?2<0) GROUP BY survey_unit_id) "
			+ ") as t", nativeQuery = true)
	Map<String,BigInteger> getStateCountByCampaignId(String campaignId, Long date);

	@Query(value = "SELECT v.organization_unit_id FROM visibility v WHERE v.campaign_id=?1", nativeQuery = true)
	List<String> findAllOrganistionUnitIdByCampaignId(String campaignId);

	@Query("SELECT new fr.insee.pearljam.api.dto.message.VerifyNameResponseDto(camp.id,  'campaign', camp.label) "
			+ "FROM Campaign camp " + "INNER JOIN Visibility vi ON vi.campaign.id = camp.id "
			+ "WHERE (vi.organizationUnit.id in (:ouIds) OR 'GUEST' in (:ouIds)) "
			+ "AND LOWER(camp.id) LIKE LOWER(concat('%',:text,'%')) " 
			+ "GROUP BY camp.id ")
	List<VerifyNameResponseDto> findMatchingCampaigns(@Param("text") String text, @Param("ouIds") List<String> ouIds,
			Pageable pageable);

}
