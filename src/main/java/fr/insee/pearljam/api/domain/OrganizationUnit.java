package fr.insee.pearljam.api.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
* Entity OrganizationUnit : represent the entity table in DB
* 
* @author Claudel Benjamin
* 
*/
@Entity
@Table
public class OrganizationUnit implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Id of the OrganizationUnit
	 */
	@Id
	@Column(length=50)
	private String id;
	
	/**
	 * The label OrganizationUnit
	 */
	@Column(length=255)
	private String label;
	
	/**
	 * The OrganizationUnitType of the OrganizationUnit
	 */
	@Enumerated(EnumType.STRING)
	@Column(length=8)
	private OrganizationUnitType type;
	
	@ManyToOne
	private OrganizationUnit organizationUnitParent;
	
	/**
	 * The visibilites of the OrganizationUnit
	 */
	@OneToMany(mappedBy = "organizationUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Visibility> visibilities;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the organisationUnitType
	 */
	public OrganizationUnitType getType() {
		return type;
	}

	/**
	 * @param organizationUnitType the organisationUnitType to set
	 */
	public void setType(OrganizationUnitType type) {
		this.type = type;
	}

	/**
	 * @return the organizationUnit
	 */
	public OrganizationUnit getOrganizationUnitParent() {
		return organizationUnitParent;
	}

	/**
	 * @param organizationUnit the organizationUnit to set
	 */
	public void setOrganizationUnitParent(OrganizationUnit organizationUnitParent) {
		this.organizationUnitParent = organizationUnitParent;
	}

	/**
	 * @return the visibilities
	 */
	public List<Visibility> getVisibilities() {
		return visibilities;
	}

	/**
	 * @param visibilities the visibilities to set
	 */
	public void setVisibilities(List<Visibility> visibilities) {
		this.visibilities = visibilities;
	}
	
	
	
}
