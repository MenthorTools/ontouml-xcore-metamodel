package net.menthor.onto2.ontouml.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.menthor.onto2.ontouml.Attribute;
import net.menthor.onto2.ontouml.Ciclicity;
import net.menthor.onto2.ontouml.Class;
import net.menthor.onto2.ontouml.ClassStereotype;
import net.menthor.onto2.ontouml.Classifier;
import net.menthor.onto2.ontouml.Container;
import net.menthor.onto2.ontouml.DataType;
import net.menthor.onto2.ontouml.DataTypeStereotype;
import net.menthor.onto2.ontouml.EndPoint;
import net.menthor.onto2.ontouml.Generalization;
import net.menthor.onto2.ontouml.GeneralizationSet;
import net.menthor.onto2.ontouml.Literal;
import net.menthor.onto2.ontouml.Model;
import net.menthor.onto2.ontouml.OntoumlFactory;
import net.menthor.onto2.ontouml.Package;
import net.menthor.onto2.ontouml.PrimitiveStereotype;
import net.menthor.onto2.ontouml.Property;
import net.menthor.onto2.ontouml.QualityNature;
import net.menthor.onto2.ontouml.Reflexivity;
import net.menthor.onto2.ontouml.Relationship;
import net.menthor.onto2.ontouml.RelationshipStereotype;
import net.menthor.onto2.ontouml.Symmetry;
import net.menthor.onto2.ontouml.Transitivity;
import net.menthor.onto2.ontouml.Type;

public class OntoumlFactoryUtil {
	
	public static OntoumlFactory factory = OntoumlFactory.eINSTANCE;

	public static Package createPackage(String name, Container ontomodel)
	{
		Package p = factory.createPackage();
		if(name!=null)p.setName(name);
		else p.setName("");
		if(ontomodel!=null) {
			ontomodel.getElements().add(p);
			p.setHolder(ontomodel);
		}
		return p;
	}
	
	public static Model createModel(String name)
	{
		Model p = factory.createModel();
		if(name!=null)p.setName(name);
		else p.setName("");
		return p;
	}
	
	public static Generalization createGeneralization (Classifier specific, Classifier general, Container container)
	{		
		Generalization g = factory.createGeneralization();		
		g.setGeneral(general);
		g.setSpecific(specific);	
		if(container!=null){
			container.getElements().add(g);
			g.setHolder(container);
		}
		return g;
	}
	
	public static GeneralizationSet createGeneralizationSet(boolean isCovering, boolean isDisjoint, Container container) 
	{
		GeneralizationSet gs = factory.createGeneralizationSet();
		gs.setIsCovering(isCovering);		
		gs.setIsCovering(isDisjoint);
		gs.setName("");
		if(container!=null) {
			container.getElements().add(gs);
			gs.setHolder(container);
		}
		return gs;
	}
	
	public static GeneralizationSet createGeneralizationSet(String name, boolean isCovering, boolean isDisjoint, Container container) 
	{
		GeneralizationSet gs = createGeneralizationSet(isCovering,isDisjoint,container);
		if(name!=null) gs.setName(name);		
		return gs;
	}
	
	public static GeneralizationSet createGeneralizationSet(String name, boolean isCovering, boolean isDisjoint, List<Generalization> gens, Container container) 
	{
		GeneralizationSet gs = createGeneralizationSet(name,isCovering,isDisjoint,container);
		gs.getGeneralizations().addAll(gens);
		for(Generalization g: gens) g.setGeneralizationSet(gs);
		return gs;
	}
		
	public static GeneralizationSet createPartition(String name, List<Classifier> specifics, Classifier general, Container container)
	{
		GeneralizationSet gs = factory.createGeneralizationSet();
		if(name!=null) gs.setName(name);
		else gs.setName("");
		gs.setIsCovering(true);
		gs.setIsDisjoint(true);		
		for(Classifier spec: specifics){
			Generalization g = createGeneralization(spec, general, container);
			g.setGeneralizationSet(gs);
			gs.getGeneralizations().add(g);			
		}
		if(container!=null){
			container.getElements().add(gs);
			gs.setHolder(container);
		}
		return gs;
	}
	
	public static Relationship createRelationship (Classifier source, Classifier target, Container container)
	{
		Relationship assoc = factory.createRelationship();		
		assoc.setStereotype(null);
		assoc.setName("");						
		assoc.setTemporalNature(null);
		assoc.setParticipationNature(null);
		assoc = setReflexivityDefault(assoc);		
		assoc = setSymmetryDefault(assoc);
		assoc = setTransitivityDefault(assoc);
		assoc = setCyclicityDefault(assoc);
		createEndPoints(assoc, source, target);
		if(container!=null){
			container.getElements().add(assoc);
			assoc.setHolder(container);
		}
		return assoc;
	}
	
	/** Create a relationship between source and target types */
	public static Relationship createRelationship (
		RelationshipStereotype stereotype, String name, Container container, 
		Classifier source, int srcLower, int srcUpper, String srcEndName, boolean isSrcDerived, boolean isSrcDependent, 
		Classifier target, int tgtLower, int tgtUpper, String tgtEndName, boolean isTgtDerived, boolean isTgtDependent
	){
		Relationship rel = createRelationship(stereotype, name, container, source, srcLower, srcUpper, target, tgtLower, tgtUpper);
		rel.sourceEnd().setName(srcEndName);
		rel.targetEnd().setName(tgtEndName);
		rel.sourceEnd().setIsDerived(isSrcDerived);
		rel.targetEnd().setIsDerived(isTgtDerived);
		rel.sourceEnd().setIsDependency(isSrcDependent);
		rel.targetEnd().setIsDependency(isTgtDependent);
		return rel;
	}
	
	/** Create a relationship between source and target types */
	public static Relationship createRelationship (RelationshipStereotype stereotype, String name, Container container, Classifier source, int srcLower, int srcUpper, Classifier target, int tgtLower, int tgtUpper)
	{
		Relationship relationship = createRelationship(stereotype,name,container);
		List<EndPoint> ends;
		if(shouldInvert(relationship,source,target)) {
			ends = createEndPoints(relationship, target, source);
			setMultiplicity(ends.get(0), tgtLower, tgtUpper);
			setMultiplicity(ends.get(1), srcLower, srcUpper);			
		} else {
			ends = createEndPoints(relationship, source, target);
			setMultiplicity(ends.get(0), srcLower, srcUpper);
			setMultiplicity(ends.get(1), tgtLower, tgtUpper);
		}		
		return relationship;
	}

	/** Create an relationship between source and target types */
	public static Relationship createRelationship (Container container, Classifier source, int srcLower, int srcUpper, String name, Classifier target, int tgtLower, int tgtUpper)
	{
		Relationship relationship = createRelationship(null,"",container);
		List<EndPoint> ends = createEndPoints(relationship, source, target);
		setMultiplicity(ends.get(0), tgtLower, tgtUpper);
		setMultiplicity(ends.get(1), srcLower, srcUpper);		
		return relationship;
	}
	
	/** Create a relationship between source and target types */
	public static Relationship createRelationship (RelationshipStereotype stereotype, Container container, Classifier source, Classifier target)
	{
		Relationship relationship = createRelationship(stereotype, "", container);
		if(shouldInvert(relationship,source,target)) {
			createEndPoints(relationship, target, source); 
		}else{
			createEndPoints(relationship, source, target);
		}		
		return relationship;
	}
	
	public static Relationship createRelationship (RelationshipStereotype stereotype, String name, Container container)
	{
		Relationship assoc = factory.createRelationship();
		assoc.setStereotype(stereotype);
		if(name!=null) assoc.setName(name);
		else assoc.setName("");		
		assoc.setTemporalNature(null);
		assoc.setParticipationNature(null);
		assoc = setReflexivityDefault(assoc);		
		assoc = setSymmetryDefault(assoc);
		assoc = setTransitivityDefault(assoc);
		assoc = setCyclicityDefault(assoc);		
		if(container!=null){
			container.getElements().add(assoc);
			assoc.setHolder(container);
		}		
		return assoc;
	}
	
	public static Relationship setCyclicityDefault(Relationship rel){
		if(rel.isMeronymic()||rel.isCharacterization()||rel.isCausation()
		|| rel.isPrecedes()||rel.isMeets()||rel.isFinishes()||rel.isStarts()||rel.isDuring()) rel.setCiclicity(Ciclicity.ACYCLIC);
		else if(rel.isOverlaps()) rel.setCiclicity(Ciclicity.NON_CYCLIC);
		else if(rel.isEquals()) rel.setCiclicity(Ciclicity.CYCLIC);
		else rel.setCiclicity(null);
		return rel;
	}
	
	public static Relationship setTransitivityDefault(Relationship rel){
		if(rel.isMemberOf()||rel.isMeets()) rel.setTransitivity(Transitivity.INTRANSITIVE);
		else if(rel.isComponentOf()||rel.isOverlaps()) rel.setTransitivity(Transitivity.NON_TRANSITIVE);
		else if(rel.isSubCollectionOf()||rel.isSubQuantityOf()||rel.isSubEventOf()||rel.isConstitution()
			  ||rel.isCharacterization()||rel.isCausation()||rel.isPrecedes()||rel.isFinishes()||rel.isStarts()
			  ||rel.isDuring()||rel.isEquals()) rel.setTransitivity(Transitivity.TRANSITIVE);
		else rel.setTransitivity(null);
		return rel;
	}
	
	public static Relationship setReflexivityDefault(Relationship rel){
		if(rel.isMemberOf()||rel.isComponentOf()||rel.isSubCollectionOf()||rel.isSubQuantityOf()) rel.setReflexivity(Reflexivity.NON_REFLEXIVE);
		else if(rel.isFinishes()||rel.isStarts()||rel.isDuring()||rel.isEquals()||rel.isOverlaps()) rel.setReflexivity(Reflexivity.REFLEXIVE);
		else if(rel.isSubEventOf()||rel.isConstitution()||rel.isMediation()||rel.isCharacterization()||rel.isCausation()) rel.setReflexivity(Reflexivity.IRREFLEXIVE);
		else rel.setReflexivity(null);
		return rel;
	}
	
	public static Relationship setSymmetryDefault(Relationship rel){
		if(rel.isMeronymic()||rel.isCharacterization()||rel.isCausation()
		|| rel.isPrecedes()||rel.isMeets()||rel.isFinishes()||rel.isStarts()) rel.setSymmetry(Symmetry.ANTI_SYMMETRIC);
		else if(rel.isDuring()) rel.setSymmetry(Symmetry.ASSYMETRIC);
		else if(rel.isEquals()||rel.isOverlaps()) rel.setSymmetry(Symmetry.SYMMETRIC);
		else rel.setSymmetry(null);
		return rel;
	}
	
	public static boolean shouldInvert(Relationship relationship, Classifier source, Classifier target)
	{
		if(relationship.isMediation() && (target instanceof Class) && ((Class)target).isTruthMaker() && (source instanceof Class) && (!((Class)source).isTruthMaker())) {
			return true;
		}
		else if(relationship.isCharacterization() && (target instanceof Class) && ((Class)target).isNonQualitativeIntrinsicMoment() && (source instanceof Class) && (!((Class)source).isNonQualitativeIntrinsicMoment())) {
			return true;
		}
		else if(relationship.isStructuration() && (target instanceof DataType) && ((DataType)target).isStructure() && (source instanceof Class) && (!((Class)source).isQuality())) {
			return true;
		}
		else if(relationship.isParticipation() && (target instanceof Class) && ((Class)target).isEvent() && (source instanceof Class) && (!((Class)source).isEvent())) {
			return true;
		}
		else if(relationship.isDerivation() && (target instanceof Relationship) && ((Relationship)target).isMaterial() && (source instanceof Class) && (!((Class)source).isTruthMaker())) {
			return true;
		}
		else if(relationship.isQuaPartOf() && (target instanceof Class) && ((Class)target).isTruthMaker() && (source instanceof Class) && (!((Class)source).isNonQualitativeIntrinsicMoment())) {
			return true;
		}
		else return false;
	}
	
	/** Create a class  */
	public static Class createClass(ClassStereotype stereotype, String name, Container container)
	{
		Class class_ = factory.createClass();
		class_.setStereotype(stereotype);
		if(class_.isMixinClass()) class_.setIsAbstract(true);
		if(name!=null) class_.setName(name);
		else class_.setName("");
		class_.setExistence(null);
		class_.setClassification(null);
		class_.setQualityNature(null);
		if(container!=null){
			container.getElements().add(class_);
			class_.setHolder(container);
		}
		return class_;
	}

	public static Class createClass(ClassStereotype stereotype, String name, boolean isAbstract, Container container)
	{
		Class class_ = factory.createClass();
		class_.setStereotype(stereotype);
		class_.setIsAbstract(isAbstract);
		if(name!=null) class_.setName(name);
		else class_.setName("");
		class_.setExistence(null);
		class_.setClassification(null);
		class_.setQualityNature(null);
		if(container!=null){
			container.getElements().add(class_);
			class_.setHolder(container);
		}
		return class_;
	}
	
	/** Create a perceivable quality */
	public static Class createPerceivableQuality(String name, Container container)
	{
		Class quality = createClass(ClassStereotype.QUALITY,name,container);
		quality.setQualityNature(QualityNature.PERCEIVABLE);		
		return quality;
	}
	
	/** Create a nonperceivable quality  */
	public static Class createNonPerceivableQuality(String name, Container container)
	{
		Class quality = createClass(ClassStereotype.QUALITY,name,container);
		quality.setQualityNature(QualityNature.NON_PERCEIVABLE);		
		return quality;
	}
	
	/** Create a nominal quality  */
	public static Class createNominalQuality(String name, Container container)
	{
		Class quality = createClass(ClassStereotype.QUALITY,name,container);
		quality.setQualityNature(QualityNature.NOMINAL);		
		return quality;
	}
		
	/** Create a data type  */
	public static DataType createDataType(DataTypeStereotype stereotype, String name, Container container)
	{
		DataType datatype = factory.createDataType();
		datatype.setStereotype(stereotype);
		if(name!=null) datatype.setName(name);
		else datatype.setName("");		
		datatype.setMeasurement(null);
		datatype.setScale(null);		
		if(container!=null){
			container.getElements().add(datatype);
			datatype.setHolder(container);
		}
		return datatype;
	}	
	
	public static Literal createLiteral(String value){
		Literal lit = factory.createLiteral();
		lit.setValue(value);
		return lit;
	}
	
	public static List<Literal> createLiterals(Collection<String> values)
	{
		List<Literal> result = new ArrayList<Literal>();
		for(String v: values){
			result.add(createLiteral(v));
		}
		return result;
	}
	
	/** Create a enumeration  */
	public static DataType createEnumeration(String name, Collection<String> values, Container container)
	{
		DataType enumeration = createDataType(DataTypeStereotype.ENUMERATION,name,container);
		List<Literal> literals = createLiterals(values);
		enumeration.getLiterals().addAll(literals);
		for(Literal lit: literals) lit.setOwner(enumeration);				
		if(container!=null){
			container.getElements().add(enumeration);
			enumeration.setHolder(container);
		}
		return enumeration;
	}	

	/**
	 * Create an attribute of this primitive to this owner type
	 * Multiplicities are by default 1..1. This is not a derived nor a dependency attribute.
	 */
	public static Attribute createAttribute (Type owner, PrimitiveStereotype primitive)
	{
		Attribute attribute = createAttribute(owner, primitive, 1, 1, "", false, false);
		if(owner!=null)owner.getAttributes().add(attribute);
		attribute.setOwner(owner);
		return attribute;		
	}
	
	/** Create an attribute of a type (class or dataType)*/
	public static Attribute createAttribute (Type owner, PrimitiveStereotype primitive, int lower, int upper)
	{
		Attribute attribute = factory.createAttribute();
		attribute.setStereotype(primitive);
		attribute.setOwner(owner);
		attribute.setLowerBound(lower);		
		attribute.setUpperBound(upper);		
		if(owner!=null && owner.getName()!=null) attribute.setName(owner.getName().trim().toLowerCase());
		else attribute.setName("");
		if(owner!=null)owner.getAttributes().add(attribute);
		attribute.setOwner(owner);
		return attribute;		
	}
		
	/** Create an attribute of a type (class or dataType) */
	public static Attribute createAttribute (Type owner, PrimitiveStereotype primitive, int lower, int upper, String name, boolean isDerived, boolean isDependency)
	{		
		Attribute attribute = createAttribute(owner,primitive, lower, upper);
		if(owner !=null && owner.getName()!=null && name==null) attribute.setName(owner.getName().trim().toLowerCase());
		else if(name!=null) attribute.setName(name);
		attribute.setIsDerived(isDerived);
		attribute.setIsDependency(isDependency);
		if(owner!=null){
			owner.getAttributes().add(attribute);
			attribute.setOwner(owner);
		}		
		return attribute;
	}

	/** Create an attribute to this classifier. */
	public static Attribute createAttribute (Type owner, PrimitiveStereotype primitive, int lower, int upper, String name, boolean isDerived)
	{				
		Attribute attribute = createAttribute(owner, primitive, lower, upper, name, isDerived, false);
		if(owner!=null)owner.getAttributes().add(attribute);
		attribute.setOwner(owner);
		return attribute;
	}
	
	/**
	 * Create an end-point 
	 * with a default name which is the classifier name in lower case 
	 */
	public static EndPoint createEndPoint(Classifier classifier, int lower, int upper) 
	{
		EndPoint endpoint = factory.createEndPoint();
		endpoint.setEndType(classifier);
		endpoint.setLowerBound(lower);
		endpoint.setUpperBound(upper);		
		if(classifier!=null && classifier.getName()!=null) endpoint.setName(classifier.getName().trim().toLowerCase());
		else endpoint.setName("");
		return endpoint;
	}

	/** Create an end-point of a relationship. */
	public static EndPoint createEndPoint(Classifier classifier, int lower, int upper, String name) 
	{
		EndPoint p = createEndPoint(classifier, lower, upper);
		if(classifier !=null && classifier.getName()!=null && name==null) p.setName(classifier.getName().trim().toLowerCase());
		else if(name!=null) p.setName(name);
		return p;
	}
	
	/** Create an end-point */
	public static EndPoint createEndPoint(Classifier classifier, int lower, int upper, String name, boolean isDerived, boolean isDependency)
	{
		EndPoint p = createEndPoint(classifier, lower, upper, name);
		p.setIsDerived(isDerived);
		p.setIsDependency(isDependency);		
		return p;
	}
	
	/** Create an end-point */
	public static EndPoint createEndPoint(Relationship rel, Classifier classifier, int lower, int upper, String name, boolean isDerived, boolean isDependency)
	{
		EndPoint p = createEndPoint(classifier, lower, upper, name, isDerived, isDependency);
		if(rel!=null){
			rel.getEndPoints().add(p);
			p.setOwner(rel);
		}
		return p;
	}
	
	/** Add end-points from and to these given types in this relationship.*/
	public static List<EndPoint> createEndPoints(Relationship relationship, Classifier source, Classifier target)
	{
		List<EndPoint> endpoints = createEndPoints(relationship);
		endpoints.get(0).setEndType(source);
		endpoints.get(1).setEndType(target);
		if(source.getName()!=null) endpoints.get(0).setName(source.getName().trim().toLowerCase());
		if(target.getName()!=null) endpoints.get(1).setName(target.getName().trim().toLowerCase());
		return endpoints;
	}
	
	public static List<EndPoint> createEndPoints(Relationship relationship) 
	{
		List<EndPoint> endpoints = new ArrayList<EndPoint>();

		EndPoint ep1;		
		if(relationship.isCharacterization() || relationship.isStructuration() || relationship.isQuaPartOf()) ep1 = createEndPoint(null, 1,1);
		else if (relationship.isSubQuantityOf()) ep1 = createEndPoint(null,0,1);
		else if (relationship.isDerivation()) ep1 = createEndPoint(null,1,-1);
		else ep1 = createEndPoint(null,0,-1);
		
		EndPoint ep2;
		if (relationship.isSubQuantityOf() || relationship.isSubCollectionOf()) ep2 = createEndPoint(null,0,1);
		else if(relationship.isMediation() || relationship.isCharacterization() || relationship.isInstanceOf() || relationship.isDerivation()) ep2 = createEndPoint(null, 1, -1);
		else  ep2 = createEndPoint(null,0,-1);
		
		//dependency
		if(relationship.isCausation() || relationship.isSubEventOf() || relationship.isTemporal() || relationship.isDerivation() || relationship.isQuaPartOf())
		{
			ep1.setIsDependency(true);
		}
		if (relationship.isCausation() || relationship.isMediation() || relationship.isSubEventOf() || relationship.isCharacterization() || 
			relationship.isParticipation() || relationship.isTemporal() || relationship.isDerivation() || relationship.isQuaPartOf()){
			ep2.setIsDependency(true);
		}
		
		//name
		String name1 = new String();
		if (ep1.getEndType() != null) 
		{
			name1 = ep1.getEndType().getName();
			if (name1 == null || name1.trim().isEmpty()) name1 = "source";
			else name1 = name1.trim().toLowerCase();
		}
		String name2 = new String();
		if (ep2.getEndType() != null) 
		{
			name2 = ep2.getEndType().getName();
			if (name2 == null || name2.trim().isEmpty()) name2 = "target";
			else name2 = name2.trim().toLowerCase();
		}
		ep1.setName(name1);
		ep2.setName(name2);
		
		//ends
		addEndPoints(relationship, ep1, ep1);
		
		return endpoints;
	}

	/** Add end points to this relationship */
	public static void addEndPoints(Relationship relationship, EndPoint sourceEnd, EndPoint targetEnd) 
	{
		relationship.getEndPoints().add(sourceEnd);
		relationship.getEndPoints().add(targetEnd);
		sourceEnd.setOwner(relationship);
		targetEnd.setOwner(relationship);
	}
	
	/** Set multiplicity of an end-point or attribute */
	public static void setMultiplicity(Property property, int lower, int upper)
	{	
		property.setLowerBound(lower);		
		property.setUpperBound(upper);			
	}

}
