package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.common.SortCriteria.SortDirection;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupOncologyOutpatientClinicMissedVisit {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties retrieved from global variables
	private Program oncologyProgram;

	private ProgramWorkflow diagnosis;
	
	private Concept scheduledVisit;
	
	private Concept biopsyResultVisit;
	
	private Concept specialVisit;
	
	private Concept telephone;
	
	private Concept telephone2;
	
	private EncounterType outpatientOncology;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "OncologyOutpatientClinicMissedVisit.xls",
		    "OncologyOutpatientClinicMissedVisit.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:7,dataset:dataset|sheet:2,row:7,dataset:dataset2|sheet:3,row:7,dataset:dataset3");
		props.put("sortWeight","5000");
		design.setProperties(props);
		
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("OncologyOutpatientClinicMissedVisit.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("ONC-Oncology Outpatient Clinic Missed Visit Patient List");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("ONC-Oncology Outpatient Clinic Missed Visit Patient List");
				
		reportDefinition.addParameter(new Parameter("endDate", "Date", Date.class));	
		reportDefinition.setBaseCohortDefinition(Cohorts.createInProgramParameterizableByDate("Oncology", oncologyProgram), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		createDataSetDefinition(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("Outpatient Missed List");
		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
		dataSetDefinition2.setName("Outpatient Biopsy Result List");
		RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
		dataSetDefinition3.setName("Outpatient Special List");
		
		dataSetDefinition.addParameter(new Parameter("endDate", "Date", Date.class));
		dataSetDefinition2.addParameter(new Parameter("endDate", "Date", Date.class));
		dataSetDefinition3.addParameter(new Parameter("endDate", "Date", Date.class));
		
		SortCriteria sortCriteria = new SortCriteria();
		sortCriteria.addSortElement("nextRDVDate", SortDirection.ASC);
		dataSetDefinition.setSortCriteria(sortCriteria);
		dataSetDefinition2.setSortCriteria(sortCriteria);
		dataSetDefinition3.setSortCriteria(sortCriteria);
		
		//Add filters
		dataSetDefinition.addFilter(Cohorts.createPatientsLateForVisit(scheduledVisit, outpatientOncology), ParameterizableUtil.createParameterMappings("endDate=${endDate-6d}"));
		dataSetDefinition2.addFilter(Cohorts.createPatientsLateForVisit(biopsyResultVisit, outpatientOncology), ParameterizableUtil.createParameterMappings("endDate=${endDate-6d}"));
		dataSetDefinition3.addFilter(Cohorts.createPatientsLateForVisit(specialVisit, outpatientOncology), ParameterizableUtil.createParameterMappings("endDate=${endDate-6d}"));
		
		//Add Columns
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecent("nextRDV", scheduledVisit, "dd/MMM/yyyy"), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getMostRecent("nextRDV", biopsyResultVisit, "dd/MMM/yyyy"), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getMostRecent("nextRDV", specialVisit, "dd/MMM/yyyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecent("nextRDVDate", scheduledVisit, "yyyy/MM/dd"), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getMostRecent("nextRDVDate", biopsyResultVisit, "yyyy/MM/dd"), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getMostRecent("nextRDVDate", specialVisit, "yyyy/MM/dd"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMiddleNameColumn("middleName"), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getMiddleNameColumn("middleName"), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getMiddleNameColumn("middleName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("id"), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getIMBId("id"), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getIMBId("id"), new HashMap<String, Object>());
		 
		dataSetDefinition.addColumn(RowPerPatientColumns.getStateOfPatient("diagnosis", oncologyProgram, diagnosis, null), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getStateOfPatient("diagnosis", oncologyProgram, diagnosis, null), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getStateOfPatient("diagnosis", oncologyProgram, diagnosis, null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecent("telephone", telephone, null), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getMostRecent("telephone", telephone, null), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getMostRecent("telephone", telephone, null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecent("telephone2", telephone2, null), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getMostRecent("telephone2", telephone2, null), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getMostRecent("telephone2", telephone2, null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("address", true, true, true, true),
		    new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getPatientAddress("address", true, true, true, true),
		    new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getPatientAddress("address", true, true, true, true),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("accompagnateur"), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getAccompRelationship("accompagnateur"), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getAccompRelationship("accompagnateur"), new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
		reportDefinition.addDataSetDefinition("dataset2", dataSetDefinition2, mappings);
		reportDefinition.addDataSetDefinition("dataset3", dataSetDefinition3, mappings);
	}
	
	private void setupProperties() {
		
		oncologyProgram = gp.getProgram(GlobalPropertiesManagement.ONCOLOGY_PROGRAM);
		
		diagnosis = gp.getProgramWorkflow(GlobalPropertiesManagement.DIAGNOSIS_WORKFLOW, GlobalPropertiesManagement.ONCOLOGY_PROGRAM);
		
		scheduledVisit = gp.getConcept(GlobalPropertiesManagement.ONCOLOGY_SCHEDULED_OUTPATIENT_VISIT);
		
		biopsyResultVisit = gp.getConcept(GlobalPropertiesManagement.ONCOLOGY_PATHOLOGY_RESULT_VISIT);
		
		specialVisit = gp.getConcept(GlobalPropertiesManagement.ONCOLOGY_SPECIAL_VISIT);
		
		telephone = gp.getConcept(GlobalPropertiesManagement.TELEPHONE_NUMBER_CONCEPT);
		
		telephone2 = gp.getConcept(GlobalPropertiesManagement.SECONDARY_TELEPHONE_NUMBER_CONCEPT);
		
		outpatientOncology = gp.getEncounterType(GlobalPropertiesManagement.OUTPATIENT_ONCOLOGY_ENCOUNTER);
	}
}
