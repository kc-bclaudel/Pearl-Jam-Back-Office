package fr.insee.pearljam.api.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
@Entity
public abstract class Address {
	/**
	* The id of Address 
	*/
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Long id;
	
	/**
	* The GeographicalLocation associated to Address 
	*/
	@ManyToOne
	protected GeographicalLocation geographicalLocation;

	public Address(){
		
	}
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the geographicalLocation
	 */
	public GeographicalLocation getGeographicalLocation() {
		return geographicalLocation;
	}
	/**
	 * @param geographicalLocation the geographicalLocation to set
	 */
	public void setGeographicalLocation(GeographicalLocation geographicalLocation) {
		this.geographicalLocation = geographicalLocation;
	}
}
