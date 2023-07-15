package io.github.xlives.framework;

import io.github.xlives.exception.ErrorCode;
import io.github.xlives.exception.JSimPiException;
import io.github.xlives.framework.unfolding.SuperRoleUnfolderManchesterSyntax;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {PreferenceProfile.class,
        SuperRoleUnfolderManchesterSyntax.class,
        OWLServiceContext.class})
public class PreferenceProfileTests {

    private static final String OWL_FILE_PATH = "family.owl";

    private static final String DEFINED_CONCEPT_1 = "Male";
    private static final String NOT_USED_CONCEPT_1 = "MA";
    private static final String FRESH_PRIMITIVE_CONCEPT_1 = "Male'";
    private static final String FRESH_PRIMITIVE_CONCEPT_2 = "Female'";
    private static final String PRIMITIVE_CONCEPT_1 = "Person";

    private static final String NOT_USED_ROLE_1 = "has";
    private static final String PRIMITIVE_ROLE_1 = "hasDaughter'";
    private static final String PRIMITIVE_ROLE_2 = "hasSon'";
    private static final String PRIMITIVE_ROLE_3 = "hasFatherInLaw'";
    private static final String ROLE_1 = "hasDaughter";

    @Autowired
    private PreferenceProfile preferenceProfile;
    @Autowired
    private OWLServiceContext OWLServiceContext;

    @Before
    public void init() {
        OWLServiceContext.init(OWL_FILE_PATH);
    }

    @After
    public void clear() {
        preferenceProfile.reset();
    }

    @Test
    public void testAddPrimitiveConceptImportanceIfDefinedConcept() {
        try {
            preferenceProfile.addPrimitiveConceptImportance(DEFINED_CONCEPT_1, new BigDecimal("10"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotPrimitiveException);
        }
    }

    @Test
    public void testAddPrimitiveConceptImportanceIfNotUsedInOntology() {
        try {
            preferenceProfile.addPrimitiveConceptImportance(NOT_USED_CONCEPT_1, new BigDecimal("10"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUsedConceptNameException);
        }
    }

    @Test
    public void testAddPrimitiveConceptImportanceIfInvalidRange() {
        try {
            preferenceProfile.addPrimitiveConceptImportance(PRIMITIVE_CONCEPT_1, new BigDecimal("-0.12"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NegativeNumberException);
        }
    }

    @Test
    public void testAddPrimitiveConceptImportanceIfValid() {
        preferenceProfile.addPrimitiveConceptImportance(FRESH_PRIMITIVE_CONCEPT_1, new BigDecimal("10"));
        Map<String, BigDecimal> primitiveConceptImportance = preferenceProfile.getPrimitiveConceptImportance();
        assertThat(primitiveConceptImportance.containsKey(FRESH_PRIMITIVE_CONCEPT_1)).isTrue();
        BigDecimal val1 = primitiveConceptImportance.get(FRESH_PRIMITIVE_CONCEPT_1);
        assertThat(val1).isEqualTo(new BigDecimal("10"));
    }

    @Test
    public void testAddRoleImportanceIfNotUsedOntology() {
        try {
            preferenceProfile.addRoleImportance(NOT_USED_ROLE_1, new BigDecimal("10"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUsedRoletNameException);
        }
    }

    @Test
    public void testAddRoleImportanceIfInvalidRange() {
        try {
            preferenceProfile.addRoleImportance(ROLE_1, new BigDecimal("-0.15"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NegativeNumberException);
        }
    }

    @Test
    public void testAddRoleImportanceIfValid() {
        preferenceProfile.addRoleImportance(ROLE_1, new BigDecimal("10"));
        BigDecimal role1Val = preferenceProfile.getRoleImportance().get(ROLE_1);
        assertThat(role1Val).isEqualTo(new BigDecimal("10"));

        preferenceProfile.addRoleImportance(PRIMITIVE_ROLE_1, new BigDecimal("5"));
        BigDecimal primitiveRole1Val = preferenceProfile.getRoleImportance().get(PRIMITIVE_ROLE_1);
        assertThat(primitiveRole1Val).isEqualTo(new BigDecimal("5"));
    }

    @Test
    public void testAddPrimitveConceptsSimilarityIfNotUsedInOntology() {
        try {
            preferenceProfile.addPrimitveConceptsSimilarity(PRIMITIVE_CONCEPT_1, NOT_USED_CONCEPT_1, new BigDecimal("0.5"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUsedConceptNameException);
        }

        try {
            preferenceProfile.addPrimitveConceptsSimilarity(NOT_USED_CONCEPT_1, PRIMITIVE_CONCEPT_1, new BigDecimal("0.5"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUsedConceptNameException);
        }
    }

    @Test
    public void testAddPrimitveConceptsSimilarityIfNotPrimitive() {
        try {
            preferenceProfile.addPrimitveConceptsSimilarity(PRIMITIVE_CONCEPT_1, DEFINED_CONCEPT_1, new BigDecimal("0.5"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotPrimitiveException);
        }

        try {
            preferenceProfile.addPrimitveConceptsSimilarity(DEFINED_CONCEPT_1, PRIMITIVE_CONCEPT_1, new BigDecimal("0.5"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotPrimitiveException);
        }
    }

    @Test
    public void testAddPrimitveConceptsSimilarityIfInvalidRange() {
        try {
            preferenceProfile.addPrimitveConceptsSimilarity(PRIMITIVE_CONCEPT_1, FRESH_PRIMITIVE_CONCEPT_1, new BigDecimal("-0.5"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUnitIntervalException);
        }

        try {
            preferenceProfile.addPrimitveConceptsSimilarity(PRIMITIVE_CONCEPT_1, FRESH_PRIMITIVE_CONCEPT_1, new BigDecimal("-1.0"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUnitIntervalException);
        }
    }

    @Test
    public void testAddPrimitveConceptsSimilarityIfValid() {
        preferenceProfile.addPrimitveConceptsSimilarity(PRIMITIVE_CONCEPT_1, FRESH_PRIMITIVE_CONCEPT_1, new BigDecimal("0.5"));
        Map<String, BigDecimal> map1Direction1 = this.preferenceProfile.getPrimitiveConceptsSimilarity().get(PRIMITIVE_CONCEPT_1);
        assertThat(map1Direction1.get(FRESH_PRIMITIVE_CONCEPT_1)).isEqualTo(new BigDecimal("0.5"));
        Map<String, BigDecimal> map1Direction2 = this.preferenceProfile.getPrimitiveConceptsSimilarity().get(FRESH_PRIMITIVE_CONCEPT_1);
        assertThat(map1Direction2.get(PRIMITIVE_CONCEPT_1)).isEqualTo(new BigDecimal("0.5"));

        preferenceProfile.addPrimitveConceptsSimilarity(FRESH_PRIMITIVE_CONCEPT_1, FRESH_PRIMITIVE_CONCEPT_2, BigDecimal.ONE);
        Map<String, BigDecimal> map2Direction1 = this.preferenceProfile.getPrimitiveConceptsSimilarity().get(FRESH_PRIMITIVE_CONCEPT_1);
        assertThat(map2Direction1.get(FRESH_PRIMITIVE_CONCEPT_2)).isEqualTo(BigDecimal.ONE);
        Map<String, BigDecimal> map2Direction2 = this.preferenceProfile.getPrimitiveConceptsSimilarity().get(FRESH_PRIMITIVE_CONCEPT_2);
        assertThat(map2Direction2.get(FRESH_PRIMITIVE_CONCEPT_1)).isEqualTo(BigDecimal.ONE);

        preferenceProfile.addPrimitveConceptsSimilarity(FRESH_PRIMITIVE_CONCEPT_2, FRESH_PRIMITIVE_CONCEPT_1, BigDecimal.ZERO);
        Map<String, BigDecimal> map3Direction1 = this.preferenceProfile.getPrimitiveConceptsSimilarity().get(FRESH_PRIMITIVE_CONCEPT_2);
        assertThat(map3Direction1.get(FRESH_PRIMITIVE_CONCEPT_1)).isEqualTo(BigDecimal.ZERO);
        Map<String, BigDecimal> map3Direction2 = this.preferenceProfile.getPrimitiveConceptsSimilarity().get(FRESH_PRIMITIVE_CONCEPT_1);
        assertThat(map3Direction2.get(FRESH_PRIMITIVE_CONCEPT_2)).isEqualTo(BigDecimal.ZERO);

        assertThat(preferenceProfile.getPrimitiveConceptsSimilarity().size()).isEqualTo(3);
        assertThat(preferenceProfile.getPrimitiveConceptsSimilarity().get(FRESH_PRIMITIVE_CONCEPT_1).size()).isEqualTo(2);
    }

    @Test
    public void testAddPrimitiveRolesSimilarityIfNotUsedInOntology() {
        try {
            preferenceProfile.addPrimitiveRolesSimilarity(NOT_USED_ROLE_1, PRIMITIVE_ROLE_1, new BigDecimal("0.5"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUsedRoletNameException);
        }

        try {
            preferenceProfile.addPrimitiveRolesSimilarity(PRIMITIVE_ROLE_1, NOT_USED_ROLE_1, new BigDecimal("0.5"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUsedRoletNameException);
        }
    }

    @Test
    public void testAddPrimitiveRolesSimilarityIfNotPrimitive() {
        try {
            preferenceProfile.addPrimitiveRolesSimilarity(ROLE_1, PRIMITIVE_ROLE_1, new BigDecimal("0.5"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotPrimitiveRoleException);
        }

        try {
            preferenceProfile.addPrimitiveRolesSimilarity(PRIMITIVE_ROLE_1, ROLE_1, new BigDecimal("0.5"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotPrimitiveRoleException);
        }
    }

    @Test
    public void testAddPrimitiveRolesSimilarityIfInvalidRange() {
        try {
            preferenceProfile.addPrimitiveRolesSimilarity(PRIMITIVE_ROLE_1, PRIMITIVE_ROLE_2, new BigDecimal("-0.1"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUnitIntervalException);
        }

        try {
            preferenceProfile.addPrimitiveRolesSimilarity(PRIMITIVE_ROLE_2, PRIMITIVE_ROLE_1, new BigDecimal("1.1"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUnitIntervalException);
        }
    }

    @Test
    public void testAddPrimitiveRolesSimilarityIfValid() {
        preferenceProfile.addPrimitiveRolesSimilarity(PRIMITIVE_ROLE_1, PRIMITIVE_ROLE_2, BigDecimal.ONE);
        Map<String, BigDecimal> map1Direction1 = preferenceProfile.getPrimitiveRolesSimilarity().get(PRIMITIVE_ROLE_1);
        assertThat(map1Direction1.get(PRIMITIVE_ROLE_2)).isEqualTo(BigDecimal.ONE);
        Map<String, BigDecimal> map1Direction2 = preferenceProfile.getPrimitiveRolesSimilarity().get(PRIMITIVE_ROLE_2);
        assertThat(map1Direction2.get(PRIMITIVE_ROLE_1)).isEqualTo(BigDecimal.ONE);

        preferenceProfile.addPrimitiveRolesSimilarity(PRIMITIVE_ROLE_2, PRIMITIVE_ROLE_1, BigDecimal.ZERO);
        Map<String, BigDecimal> map2Direction1 = preferenceProfile.getPrimitiveRolesSimilarity().get(PRIMITIVE_ROLE_2);
        assertThat(map2Direction1.get(PRIMITIVE_ROLE_1)).isEqualTo(BigDecimal.ZERO);
        Map<String, BigDecimal> map2Direction2 = preferenceProfile.getPrimitiveRolesSimilarity().get(PRIMITIVE_ROLE_1);
        assertThat(map2Direction2.get(PRIMITIVE_ROLE_2)).isEqualTo(BigDecimal.ZERO);

        preferenceProfile.addPrimitiveRolesSimilarity(PRIMITIVE_ROLE_1, PRIMITIVE_ROLE_2, new BigDecimal("0.5"));
        Map<String, BigDecimal> map3Direction1 = preferenceProfile.getPrimitiveRolesSimilarity().get(PRIMITIVE_ROLE_1);
        assertThat(map3Direction1.get(PRIMITIVE_ROLE_2)).isEqualTo(new BigDecimal("0.5"));
        Map<String, BigDecimal> map3Direction2 = preferenceProfile.getPrimitiveRolesSimilarity().get(PRIMITIVE_ROLE_2);
        assertThat(map3Direction2.get(PRIMITIVE_ROLE_1)).isEqualTo(new BigDecimal("0.5"));

        preferenceProfile.addPrimitiveRolesSimilarity(PRIMITIVE_ROLE_1, PRIMITIVE_ROLE_3, new BigDecimal("0.1"));
        Map<String, BigDecimal> map4Direction1 = preferenceProfile.getPrimitiveRolesSimilarity().get(PRIMITIVE_ROLE_1);
        assertThat(map4Direction1.get(PRIMITIVE_ROLE_3)).isEqualTo(new BigDecimal("0.1"));
        Map<String, BigDecimal> map4Direction2 = preferenceProfile.getPrimitiveRolesSimilarity().get(PRIMITIVE_ROLE_3);
        assertThat(map4Direction2.get(PRIMITIVE_ROLE_1)).isEqualTo(new BigDecimal("0.1"));

        assertThat(map4Direction1.get(PRIMITIVE_ROLE_3)).isNotEqualTo(new BigDecimal("0.5"));
        assertThat(map4Direction2.get(PRIMITIVE_ROLE_1)).isNotEqualTo(new BigDecimal("0.5"));

        assertThat(preferenceProfile.getPrimitiveRolesSimilarity().size()).isEqualTo(3);
        assertThat(preferenceProfile.getPrimitiveRolesSimilarity().get(PRIMITIVE_ROLE_1).size()).isEqualTo(2);
    }

    @Test
    public void testAddRoleDiscountFactorIfNotUsedInOntology() {
        try {
            preferenceProfile.addRoleDiscountFactor(NOT_USED_ROLE_1, BigDecimal.ONE);
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUsedRoletNameException);
        }
    }

    @Test
    public void testAddRoleDiscountFactorIfInvalidRange() {
        try {
            preferenceProfile.addRoleDiscountFactor(ROLE_1, new BigDecimal("1.1"));
        }

        catch (JSimPiException e) {
            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PreferenceProfile_NotUnitIntervalException);

        }
    }

    @Test
    public void testAddRoleDiscountFactorIfValid() {
        preferenceProfile.addRoleDiscountFactor(PRIMITIVE_ROLE_1, BigDecimal.ZERO);
        assertThat(preferenceProfile.getRoleDiscountFactor().get(PRIMITIVE_ROLE_1)).isEqualTo(BigDecimal.ZERO);

        preferenceProfile.addRoleDiscountFactor(PRIMITIVE_ROLE_1, BigDecimal.ONE);
        assertThat(preferenceProfile.getRoleDiscountFactor().get(PRIMITIVE_ROLE_1)).isEqualTo(BigDecimal.ONE);

        preferenceProfile.addRoleDiscountFactor(PRIMITIVE_ROLE_1, new BigDecimal("0.5"));
        assertThat(preferenceProfile.getRoleDiscountFactor().get(PRIMITIVE_ROLE_1)).isEqualTo(new BigDecimal("0.5"));

        preferenceProfile.addRoleDiscountFactor(ROLE_1, new BigDecimal("0.2"));
        assertThat(preferenceProfile.getRoleDiscountFactor().get(ROLE_1)).isEqualTo(new BigDecimal("0.2"));

        assertThat(preferenceProfile.getRoleDiscountFactor().size()).isEqualTo(2);
    }

}
