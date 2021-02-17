package io.elimu.a2d2.fhir;

import java.text.SimpleDateFormat;

import org.hl7.fhir.convertors.NullVersionConverterAdvisor40;
import org.hl7.fhir.convertors.VersionConvertor_10_40;
import org.hl7.fhir.convertors.VersionConvertor_30_40;

import ca.uhn.fhir.context.FhirContext;

public class FhirTransformHelper {

	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	private static org.hl7.fhir.r4.model.MedicationRequest transformMedicationOrder(ca.uhn.fhir.model.dstu2.resource.MedicationOrder mo) {
		org.hl7.fhir.r4.model.MedicationRequest retval = new org.hl7.fhir.r4.model.MedicationRequest();
		retval.setId(mo.getId() != null ? mo.getIdElement().getIdPart() : null);
		retval.getMeta().setVersionId(mo.getMeta().getVersionId());
		retval.getMeta().setLastUpdated(mo.getMeta().getLastUpdated());
		retval.setAuthoredOn(mo.getDateWritten());
		retval.setStatus(org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestStatus.fromCode(mo.getStatus()));
		if (mo.getMedication() != null) {
			retval.getMedicationReference().setReference(((ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt) mo.getMedication()).getReference().getValue());
			retval.getMedicationReference().setDisplay(((ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt) mo.getMedication()).getDisplay().getValue());
		}
		boolean alreadyDated = false;
		for (ca.uhn.fhir.model.dstu2.resource.MedicationOrder.DosageInstruction di : mo.getDosageInstruction()) {
			org.hl7.fhir.r4.model.Dosage di4 = retval.addDosageInstruction();
			if (!alreadyDated) {
				if (mo.getDateWritten() != null) {
					di.getAdditionalInstructions().addCoding().setSystem("urn:oid:date-written").setDisplay(FORMAT.format(mo.getDateWritten()));
				}
				if (mo.getDateEnded() != null) {
					di.getAdditionalInstructions().addCoding().setSystem("urn:oid:date-ended").setDisplay(FORMAT.format(mo.getDateWritten()));
				}
			}
			alreadyDated = true;
			di4.setText(di.getText());
			if (di.getDose() != null) {
				di4.getDoseAndRateFirstRep().getDoseQuantity().setValue(((ca.uhn.fhir.model.dstu2.composite.QuantityDt) di.getDose()).getValue());
			}
			for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : di.getRoute().getCoding()) {
				di4.getRoute().addCoding().setSystem(c.getSystem()).setCode(c.getCode()).setDisplay(c.getDisplay());
			}
		}
		retval.getDispenseRequest().getQuantity().setValue(mo.getDispenseRequest().getQuantity().getValue()).setUnit(mo.getDispenseRequest().getQuantity().getUnit());
		for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : mo.getSubstitution().getReason().getCoding()) {
			retval.getSubstitution().getReason().addCoding().setSystem(c.getSystem()).setCode(c.getCode()).setDisplay(c.getDisplay());
		}
		retval.getSubject().setReference(mo.getPatient().getReference().getIdPart()).setDisplay(mo.getPatient().getDisplay().getValue());
		retval.getPerformer().setReference(mo.getPrescriber().getReference().getIdPart()).setDisplay(mo.getPrescriber().getDisplay().getValue());
		return retval;
	}
	
	public static org.hl7.fhir.r4.model.AllergyIntolerance transformAllergyIntolerance(ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance ai) {
		if (ai == null) {
			return null;
		}
		org.hl7.fhir.r4.model.AllergyIntolerance retval = new org.hl7.fhir.r4.model.AllergyIntolerance();
		retval.setId(ai.getId() == null ? null : ai.getId().getIdPart());
		retval.getMeta().setVersionId(ai.getMeta().getVersionId());
		retval.getMeta().setLastUpdated(ai.getMeta().getLastUpdated());
		if (ai.getText() != null && ai.getText().getDiv() != null) {
			retval.getText().getDiv().setContent(ai.getText().getDiv().getValue());
		}
		if (ai.getOnset() != null) {
			retval.setOnset(new org.hl7.fhir.r4.model.DateTimeType(ai.getOnset()));
		}
		retval.getRecorder().setReference(ai.getRecorder().getReference().getValue());
		retval.getRecorder().setDisplay(ai.getRecorder().getDisplay().getValue());
		retval.getPatient().setReference(ai.getPatient().getReference().getValue());
		retval.setLastOccurrence(ai.getLastOccurence());
		for (ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance.Reaction r : ai.getReaction()) {
			org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceReactionComponent r4 = retval.addReaction();
			for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : r.getSubstance().getCoding()) {
				r4.getSubstance().addCoding().setSystem(c.getSystem()).setCode(c.getCode()).setDisplay(c.getDisplay());
			}
			for (ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt cpt : r.getManifestation()) {
				org.hl7.fhir.r4.model.CodeableConcept c4 = r4.addManifestation();
				for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : cpt.getCoding()) {
					c4.addCoding().setSystem(c.getSystem()).setCode(c.getCode()).setDisplay(c.getDisplay());
				}
			}
			r4.setSeverity(org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceSeverity.fromCode(r.getSeverity()));
			if (r.getCertainty() != null) {
				r4.setDescription("Certainty: " + r.getCertainty());
			}
		}
		for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : ai.getSubstance().getCoding()) {
			retval.getCode().addCoding().setSystem(c.getSystem()).setCode(c.getCode()).setDisplay(c.getDisplay());
		}
		return retval;
	}

	public static org.hl7.fhir.r4.model.Resource transformDstu3Resource(org.hl7.fhir.dstu3.model.Resource v3res) {
		return VersionConvertor_30_40.convertResource(v3res, true);
	}

	public static org.hl7.fhir.r4.model.Resource transformDstu2Resource(ca.uhn.fhir.model.dstu2.resource.BaseResource v2resource) {
		if (v2resource instanceof ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance) {
			return transformAllergyIntolerance((ca.uhn.fhir.model.dstu2.resource.AllergyIntolerance) v2resource);
		} else if (v2resource instanceof ca.uhn.fhir.model.dstu2.resource.MedicationOrder) {
			return transformMedicationOrder((ca.uhn.fhir.model.dstu2.resource.MedicationOrder) v2resource);
		}
		
		String dstu2json = FhirContext.forDstu2().newJsonParser().encodeResourceToString(v2resource);
		//For Patient: AdministrativeGender will receive Female, Male, and expect female, male. We need to pre-convert
		dstu2json = dstu2json.replaceAll("\"Female\"", "\"female\"");
		dstu2json = dstu2json.replaceAll("\"Male\"", "\"male\"");
		//For Mer
		org.hl7.fhir.instance.model.Resource v2hl7resource = (org.hl7.fhir.instance.model.Resource) FhirContext.forDstu2Hl7Org().newJsonParser().parseResource(dstu2json);
		org.hl7.fhir.r4.model.Resource v4resource = new VersionConvertor_10_40(new NullVersionConverterAdvisor40()).convertResource(v2hl7resource);
		return v4resource;
	}
}
