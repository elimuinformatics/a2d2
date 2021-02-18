package io.elimu.a2d2.fhir;

import java.text.SimpleDateFormat;

import org.hl7.fhir.convertors.NullVersionConverterAdvisor40;
import org.hl7.fhir.convertors.VersionConvertor_10_40;
import org.hl7.fhir.convertors.VersionConvertor_30_40;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class FhirTransformHelper {

	private static final Logger log = LoggerFactory.getLogger(FhirTransformHelper.class);
	
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	private static org.hl7.fhir.r4.model.Procedure transformProcedure(ca.uhn.fhir.model.dstu2.resource.Procedure pr) {
		org.hl7.fhir.r4.model.Procedure retval = new org.hl7.fhir.r4.model.Procedure();
		for (ca.uhn.fhir.model.dstu2.composite.IdentifierDt id : pr.getIdentifier()) {
			org.hl7.fhir.r4.model.Identifier id4 = retval.addIdentifier();
			if (id.getValue() != null) {
				id4.setValue(id.getValue());
			}
			if (id.getPeriod() != null) {
				id4.getPeriod().setEnd(id.getPeriod().getEnd());
				id4.getPeriod().setStart(id.getPeriod().getStart());
			}
			id4.setSystem(id.getSystem());
			id4.getType().setText(id.getType().getText());
			for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : id.getType().getCoding()) {
				id4.getType().addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
			}
		}
		retval.setId(pr.getId().getIdPart());
		retval.getMeta().setVersionId(pr.getMeta().getVersionId());
		retval.getMeta().setLastUpdated(pr.getMeta().getLastUpdated());
		for (ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt bs : pr.getBodySite()) {
			org.hl7.fhir.r4.model.CodeableConcept bs4 = retval.addBodySite();
			bs4.setText(bs.getText());
			for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : bs.getCoding()) {
				bs4.addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
			}
		}
		retval.getCategory().setText(pr.getCategory().getText());
		for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : pr.getCategory().getCoding()) {
			retval.getCategory().addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
		}
		retval.getCode().setText(pr.getCode().getText());
		for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : pr.getCode().getCoding()) {
			retval.getCode().addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
		}
		for (ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt comp : pr.getComplication()) {
			org.hl7.fhir.r4.model.CodeableConcept comp4 = retval.addComplication();
			comp4.setText(comp.getText());
			for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : comp.getCoding()) {
				comp4.addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
			}
		}
		if (pr.getEncounter().getReference().getIdPart() != null) {
			retval.getEncounter().setReference(pr.getEncounter().getReference().getIdPart());
		}
		if (pr.getEncounter().getDisplay() != null) {
			retval.getEncounter().setDisplay(pr.getEncounter().getDisplay().getValue());
		}
		for (ca.uhn.fhir.model.dstu2.resource.Procedure.FocalDevice fd : pr.getFocalDevice()) {
			org.hl7.fhir.r4.model.Procedure.ProcedureFocalDeviceComponent fd4 = retval.addFocalDevice();
			fd4.getAction().setText(fd.getAction().getText());
			for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : fd.getAction().getCoding()) {
				fd4.getAction().addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
			}
			if (fd.getManipulated().getReference().getIdPart() != null) {
				fd4.getManipulated().setReference(fd.getManipulated().getReference().getIdPart());
			}
			if (fd.getManipulated().getDisplay() != null) {
				fd4.getManipulated().setDisplay(fd.getManipulated().getDisplay().getValue());
			}
		}
		for (ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt fu : pr.getFollowUp()) {
			org.hl7.fhir.r4.model.CodeableConcept fu4 = retval.addFollowUp();
			fu4.setText(fu.getText());
			for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : fu.getCoding()) {
				fu4.addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
			}
		}
		if (pr.getLocation().getReference().getIdPart() != null) {
			retval.getLocation().setReference(pr.getLocation().getReference().getIdPart());
		}
		if (pr.getLocation().getDisplay() != null) {
			retval.getLocation().setDisplay(pr.getLocation().getDisplay().getValue());
		}
		for (ca.uhn.fhir.model.dstu2.composite.AnnotationDt an : pr.getNotes()) {
			org.hl7.fhir.r4.model.Annotation an4 = retval.addNote();
			if (an.getAuthor() instanceof ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt) {
				ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt ref = (ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt) an.getAuthor();
				org.hl7.fhir.r4.model.Reference ref4 = new org.hl7.fhir.r4.model.Reference();
				an4.setAuthor(ref4);
				if (ref.getReference().getIdPart() != null) {
					ref4.setReference(ref.getReference().getIdPart());
				}
				if (ref.getDisplay() != null) {
					ref4.setDisplay(ref.getDisplay().getValue());
				}
			} else if (an.getAuthor() instanceof ca.uhn.fhir.model.primitive.StringDt) {
				ca.uhn.fhir.model.primitive.StringDt str = (ca.uhn.fhir.model.primitive.StringDt) an.getAuthor();
				an4.setAuthor(new org.hl7.fhir.r4.model.StringType(str.getValue()));
			} else {
				log.warn("Procedure.notes.author unexpected type: " + an.getAuthor().getClass().getName());
			}
			an4.setText(an.getText());
			an4.setTime(an.getTime());
		}
		retval.getOutcome().setText(pr.getOutcome().getText());
		for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : pr.getOutcome().getCoding()) {
			retval.getOutcome().addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
		}
		if (pr.getPerformed() != null) {
			if (pr.getPerformed() instanceof ca.uhn.fhir.model.dstu2.composite.AgeDt) {
				ca.uhn.fhir.model.dstu2.composite.AgeDt a = (ca.uhn.fhir.model.dstu2.composite.AgeDt) pr.getPerformed();
				org.hl7.fhir.r4.model.Age a4 = new org.hl7.fhir.r4.model.Age();
				a4.setCode(a.getCode());
				if (a.getComparator() != null) {
					try {
						a4.setComparator(org.hl7.fhir.r4.model.Quantity.QuantityComparator.fromCode(a.getComparator()));
					} catch (org.hl7.fhir.exceptions.FHIRException e) { /* ignore */ }
				}
				a4.setSystem(a.getSystem());
				a4.setUnit(a.getUnit());
				a4.setValue(a.getValue());
				retval.setPerformed(a4);
			} else if (pr.getPerformed() instanceof ca.uhn.fhir.model.primitive.DateTimeDt) {
				ca.uhn.fhir.model.primitive.DateTimeDt d = (ca.uhn.fhir.model.primitive.DateTimeDt) pr.getPerformed();
				retval.setPerformed(new org.hl7.fhir.r4.model.DateTimeType(d.getValue()));
			} else if (pr.getPerformed() instanceof ca.uhn.fhir.model.dstu2.composite.PeriodDt) {
				ca.uhn.fhir.model.dstu2.composite.PeriodDt p = (ca.uhn.fhir.model.dstu2.composite.PeriodDt) pr.getPerformed();
				org.hl7.fhir.r4.model.Period p4 = new org.hl7.fhir.r4.model.Period();
				p4.setStart(p.getStart());
				p4.setEnd(p.getEnd());
				retval.setPerformed(p4);
			} else if (pr.getPerformed() instanceof ca.uhn.fhir.model.dstu2.composite.RangeDt) {
				ca.uhn.fhir.model.dstu2.composite.RangeDt r = (ca.uhn.fhir.model.dstu2.composite.RangeDt) pr.getPerformed();
				org.hl7.fhir.r4.model.Range r4 = new org.hl7.fhir.r4.model.Range();
				r4.setHigh(toR4Quantity(r.getHigh()));
				r4.setLow(toR4Quantity(r.getLow()));
				retval.setPerformed(r4);
			} else if (pr.getPerformed() instanceof ca.uhn.fhir.model.primitive.StringDt) {
				ca.uhn.fhir.model.primitive.StringDt str = (ca.uhn.fhir.model.primitive.StringDt) pr.getPerformed();
				retval.setPerformed(new org.hl7.fhir.r4.model.StringType(str.getValue()));
			} else {
				log.warn("Procedure.performed unexpected type: " + pr.getPerformed().getClass().getName());
			}
		}
		if (pr.getReason() != null) {
			if (pr.getReason() instanceof ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt) {
				ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt ref = (ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt) pr.getReason();
				org.hl7.fhir.r4.model.Reference ref4 = retval.addReasonReference();
				if (ref.getReference().getIdPart() != null) {
					ref4.setReference(ref.getReference().getIdPart());
				}
				if (ref.getDisplay() != null) {
					ref4.setDisplay(ref.getDisplay().getValue());
				}
			} else if (pr.getReason() instanceof ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt) {
				ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt cpt = (ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt) pr.getReason();
				org.hl7.fhir.r4.model.CodeableConcept cpt4 = retval.addReasonCode();
				cpt4.setText(cpt.getText());
				for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : cpt.getCoding()) {
					cpt4.addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
				}
			}
		}
		if (pr.getStatus() != null) {
			if ("aborted".equalsIgnoreCase(pr.getStatus())) {
				retval.setStatus(org.hl7.fhir.r4.model.Procedure.ProcedureStatus.STOPPED);
			} else {
				try {
					retval.setStatus(org.hl7.fhir.r4.model.Procedure.ProcedureStatus.fromCode(pr.getStatus()));
				} catch (org.hl7.fhir.exceptions.FHIRException e) {
					log.warn("Procedure.status unexpected value: " + pr.getStatus());
				}
			}
		}
		if (pr.getSubject().getReference().getIdPart() != null) {
			retval.getSubject().setReference(pr.getSubject().getReference().getIdPart());
		}
		if (pr.getSubject().getDisplay() != null) {
			retval.getSubject().setDisplay(pr.getSubject().getDisplay().getValue());
		}
		ca.uhn.fhir.model.dstu2.composite.NarrativeDt n = pr.getText();
		retval.getText().setDivAsString(n.getDivAsString());
		if (n.getStatus() != null) {
			try {
				retval.getText().setStatus(org.hl7.fhir.r4.model.Narrative.NarrativeStatus.fromCode(n.getStatusAsString()));
			} catch (org.hl7.fhir.exceptions.FHIRException e) {
				log.warn("Procedure.text.status unexpected value: " + pr.getStatus());
			}
		}
		for (ca.uhn.fhir.model.dstu2.resource.Procedure.Performer p : pr.getPerformer()) {
			org.hl7.fhir.r4.model.Procedure.ProcedurePerformerComponent p4 = retval.addPerformer();
			if (p.getActor().getReference().getIdPart() != null) {
				p4.getActor().setReference(p.getActor().getReference().getIdPart());
			}
			if (p.getActor().getDisplay() != null) {
				p4.getActor().setDisplay(p.getActor().getDisplay().getValue());
			}
			p4.getFunction().setText(p.getRole().getText());
			for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : p.getRole().getCoding()) {
				p4.getFunction().addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
			}
		}
		pr.getPerformer();
		for (ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt ref : pr.getReport()) {
			org.hl7.fhir.r4.model.Reference ref4 = retval.addReport();
			if (ref.getReference().getIdPart() != null) {
				ref4.setReference(ref.getReference().getIdPart());
			}
			if (ref.getDisplay() != null) {
				ref4.setDisplay(ref.getDisplay().getValue());
			}
		}
		for (ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt ref : pr.getUsed()) {
			org.hl7.fhir.r4.model.Reference ref4 = retval.addUsedReference();
			if (ref.getReference().getIdPart() != null) {
				ref4.setReference(ref.getReference().getIdPart());
			}
			if (ref.getDisplay() != null) {
				ref4.setDisplay(ref.getDisplay().getValue());
			}
		}
		return retval;
	}
	
	private static org.hl7.fhir.r4.model.Quantity toR4Quantity(ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt qty) {
		org.hl7.fhir.r4.model.Quantity retval = new org.hl7.fhir.r4.model.Quantity();
		retval.setCode(qty.getCode());
		if (qty.getComparator() != null) {
			try {
				retval.setComparator(org.hl7.fhir.r4.model.Quantity.QuantityComparator.fromCode(qty.getComparator()));
			} catch (org.hl7.fhir.exceptions.FHIRException e) { /* ignore */ }
		}
		retval.setSystem(qty.getSystem());
		retval.setUnit(qty.getUnit());
		retval.setValue(qty.getValue());
		return retval;
	}

	private static org.hl7.fhir.r4.model.MedicationRequest transformMedicationOrder(ca.uhn.fhir.model.dstu2.resource.MedicationOrder mo) {
		org.hl7.fhir.r4.model.MedicationRequest retval = new org.hl7.fhir.r4.model.MedicationRequest();
		retval.setId(mo.getId() != null ? mo.getIdElement().getIdPart() : null);
		retval.getMeta().setVersionId(mo.getMeta().getVersionId());
		retval.getMeta().setLastUpdated(mo.getMeta().getLastUpdated());
		retval.setAuthoredOn(mo.getDateWritten());
		retval.setStatus(org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestStatus.fromCode(mo.getStatus()));
		for (ca.uhn.fhir.model.dstu2.composite.IdentifierDt id : mo.getIdentifier()) {
			org.hl7.fhir.r4.model.Identifier id4 = retval.addIdentifier();
			if (id.getValue() != null) {
				id4.setValue(id.getValue());
			}
			if (id.getPeriod() != null) {
				id4.getPeriod().setEnd(id.getPeriod().getEnd());
				id4.getPeriod().setStart(id.getPeriod().getStart());
			}
			id4.setSystem(id.getSystem());
			id4.getType().setText(id.getType().getText());
			for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : id.getType().getCoding()) {
				id4.getType().addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
			}
		}
		if (mo.getMedication() != null) {
			if (mo.getMedication() instanceof ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt) {
				retval.getMedicationReference().setReference(((ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt) mo.getMedication()).getReference().getValue());
				retval.getMedicationReference().setDisplay(((ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt) mo.getMedication()).getDisplay().getValue());
			} else if (mo.getMedication() instanceof ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt) {
				retval.getMedicationReference().setReference(((ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt) mo.getMedication()).getCodingFirstRep().getCode());
				retval.getMedicationReference().setDisplay(((ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt) mo.getMedication()).getText());
			} else {
				log.warn("MedicationOrder.medication of unexpected type: " + mo.getMedication().getClass().getName());
			}
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
				if (di.getDose() instanceof ca.uhn.fhir.model.dstu2.composite.QuantityDt) {
					di4.getDoseAndRateFirstRep().getDoseQuantity().setValue(((ca.uhn.fhir.model.dstu2.composite.QuantityDt) di.getDose()).getValue());
				} else {
					log.warn("MedicationOrder.doseInstruction.dose unexpected type: " + di.getDose().getClass().getName());
				}
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
		for (ca.uhn.fhir.model.dstu2.composite.IdentifierDt id : ai.getIdentifier()) {
			org.hl7.fhir.r4.model.Identifier id4 = retval.addIdentifier();
			if (id.getValue() != null) {
				id4.setValue(id.getValue());
			}
			if (id.getPeriod() != null) {
				id4.getPeriod().setEnd(id.getPeriod().getEnd());
				id4.getPeriod().setStart(id.getPeriod().getStart());
			}
			id4.setSystem(id.getSystem());
			id4.getType().setText(id.getType().getText());
			for (ca.uhn.fhir.model.dstu2.composite.CodingDt c : id.getType().getCoding()) {
				id4.getType().addCoding().setCode(c.getCode()).setSystem(c.getSystem()).setDisplay(c.getDisplay());
			}
		}retval.getRecorder().setReference(ai.getRecorder().getReference().getValue());
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
		} else if (v2resource instanceof ca.uhn.fhir.model.dstu2.resource.Procedure) {
			return transformProcedure((ca.uhn.fhir.model.dstu2.resource.Procedure) v2resource);
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
