package fr.insee.pearljam.api.bussinessrules;

import fr.insee.pearljam.api.domain.StateType;

public class BussinessRules {
	
	// Checks if a survey unit is allowed to pass from a state to another
	public static Boolean stateCanBeModified(StateType currentState, StateType targetState) {
		switch(targetState) {
			case NVI: return currentState != StateType.NVI;
			case ANS: return currentState == StateType.NNS;
			case FIN: return currentState == StateType.TBR || currentState == StateType.WFS;
			case WFT: return currentState == StateType.FIN || currentState == StateType.TBR
					|| currentState == StateType.INS;
			case PRC: return currentState == StateType.VIC || currentState == StateType.ANS;
			case AOC: return currentState == StateType.APS || currentState == StateType.PRC;
			case APS: return currentState == StateType.AOC || currentState == StateType.PRC;
			case INS: return currentState == StateType.APS || currentState == StateType.PRC 
					|| currentState == StateType.FIN || currentState == StateType.TBR;
			case WFS: return currentState == StateType.WFT;
			case TBR: return currentState == StateType.WFS;
			case VIC: return currentState == StateType.ANS;
			case NVM: return currentState == StateType.NVI;
			default: return false;
		}
	}
	
	/* Checks if a survey unit is allowed to pass from a state to another
	 * via a manager action
	 */
	public static Boolean stateCanBeModifiedByManager(StateType currentState, StateType targetState) {
		switch(targetState) {
			case NVI: return currentState != StateType.NVI;
			case ANS: return currentState == StateType.NNS;
			case FIN: return currentState == StateType.TBR;
			case WFT: return currentState == StateType.FIN || currentState == StateType.TBR;
			default: return false;
		}
	}
	
	/* Checks if a survey unit is allowed to pass from a state to another
	 * via an interviewer action
	 */
	public static Boolean stateCanBeModifiedByInterviewer(StateType currentState, StateType targetState) {
		switch(targetState) {
			case PRC: return currentState == StateType.VIC || currentState == StateType.ANS;
			case AOC: return currentState == StateType.APS || currentState == StateType.PRC;
			case APS: return currentState == StateType.AOC || currentState == StateType.PRC;
			case INS: return currentState == StateType.APS || currentState == StateType.PRC 
					|| currentState == StateType.FIN || currentState == StateType.TBR;
			case WFT: return currentState == StateType.INS;
			case WFS: return currentState == StateType.WFT;
			case TBR: return currentState == StateType.WFS;
			case FIN: return currentState == StateType.WFS;
			default: return false;
		}
	}
	
	/* Checks if a survey unit is allowed to pass from a state to another
	 * via an automatic bussiness rule
	 */
	public static Boolean stateCanBeModifiedBussinessRules(StateType currentState, StateType targetState) {
		switch(targetState) {
			case VIC: return currentState == StateType.ANS;
			case WFT: return currentState == StateType.INS;			
			case FIN: return currentState == StateType.WFS;
			case TBR: return currentState == StateType.WFS;
			case NVI: return currentState == StateType.FIN;
			case NVM: return currentState == StateType.NVI;
			default: return false;
		}
	}
	
	/* Checks if a survey unit can be seen by the interviewer
	 * via an automatic bussiness rule
	 */
	public static Boolean stateCanBeSeenByInterviewerBussinessRules(StateType currentState) {
		switch(currentState) {
			case NNS: return false;
			case NVI: return false;			
			case NVM: return false;
			default: return true;
		}
	}
}
