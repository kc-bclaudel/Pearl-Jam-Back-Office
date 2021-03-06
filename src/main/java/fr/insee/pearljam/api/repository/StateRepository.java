package fr.insee.pearljam.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.insee.pearljam.api.domain.State;
import fr.insee.pearljam.api.domain.SurveyUnit;
import fr.insee.pearljam.api.dto.state.StateDto;

/**
* StateRepository is the repository using to access to State table in DB
* 
* @author Claudel Benjamin
* 
*/
public interface StateRepository extends JpaRepository<State, Long> {
	
	/**
	 * This method retrieve the State in db the SurveyUnit associated
	 * @param SurveyUnit
	 * @return StateDto
	 */
	StateDto findFirstDtoBySurveyUnitOrderByDateDesc(SurveyUnit surveyUnit);

	List<StateDto> findAllDtoBySurveyUnitIdOrderByDateAsc(String suId);

	Optional<StateDto> findDtoById(Long long1);
	
	/**
	 * This method retrieve the last State in db the SurveyUnitId associated
	 * @param idSurveyUnit
	 * @return State type
	 */
	StateDto findFirstDtoBySurveyUnitIdOrderByDateDesc(String surveyUnitId);

	@Query(value="SELECT MAX(s.date) as date FROM state s " + 
			"WHERE s.type='FIN' AND s.survey_unit_id=?1 ", nativeQuery=true)
	Long findFinDateBySurveyUnitId(String idSurveyUnit);

}
