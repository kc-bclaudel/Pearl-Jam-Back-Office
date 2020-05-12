package fr.insee.pearljam.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.insee.pearljam.api.domain.Interviewer;

/**
* InterviewerRepository is the repository using to access to Interviewer table in DB
* 
* @author Claudel Benjamin
* 
*/
public interface InterviewerRepository extends JpaRepository<Interviewer, String> {

}