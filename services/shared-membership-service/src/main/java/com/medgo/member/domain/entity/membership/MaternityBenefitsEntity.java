package com.medgo.member.domain.entity.membership;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "SYS_RSP_MTBL")
@NamedStoredProcedureQuery(
        name = "SP.getMaternityBenefitsByModelEntity",
        procedureName = "SP_SELECTRSP",
        resultClasses = MaternityBenefitsEntity.class,
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "ACCOUNTCODE", type = String.class)
        }
)
public class MaternityBenefitsEntity {
    @Id
    @Column(name = "PRE_FORBILLING")
    private Integer preForBilling;

    @Column(name = "NO_EMP")
    private String noEmp;

    @Column(name = "NO_EMP_DEP")
    private String noEmpDep;

    @Column(name = "ELIG_STD")
    private Boolean eligStd;

    @Column(name = "PRINCIPAL")
    private String principal;

    @Column(name = "DEP_SPOUSE")
    private String depSpouse;

    @Column(name = "DEP_CHILDREN")
    private String depChildren;

    @Column(name = "DEP_PARENTS")
    private String depParents;

    @Column(name = "DEP_BROS_SIS")
    private String depBrosSis;

    @Column(name = "DEP_NOTE")
    private String depNote;

    @Column(name = "MP_STD_ITG")
    private Boolean mpStgItg;

    @Column(name = "MP_EXCLUSIVE")
    private Boolean mpExclusive;

    @Column(name = "MP_RM_BOARD")
    private String mpRmBoard;

    @Column(name = "MP_AUTO")
    private Boolean mpAuto;

    @Column(name = "MAB_FOR")
    private String mabFor;

    @Column(name = "MAB_INC_PREMIUM")
    private Boolean mabIncPremium;

    @Column(name = "MAB_OPTIONAL")
    private String mabOptional;

    @Column(name = "MAB_OPT_PAYABLE")
    private String mabOptPayable;

    @Column(name = "MAB_NO_MARRIED_FML")
    private String mabNoMarriedFml;

    @Column(name = "MAB_NO_MARRIED_ML")
    private String mabNoMarriedMl;

    @Column(name = "MAB_NORM_DELIVERY")
    private String mabNormDelivery;

    @Column(name = "MAB_CAEAREAN")
    private String mabCaearean;

    @Column(name = "MAB_ABORT")
    private String mabAbort;

    @Column(name = "MAB_HOME_DELIVERY")
    private String mabHomeDelivery;

    @Column(name = "MAB_ABNORMAL")
    private String mabAbnormal;

    @Column(name = "MAB_OTHERS")
    private String mabOthers;

    @Column(name = "ECU_OUT")
    private Boolean ecuOut;

    @Column(name = "ECU_INCL")
    private Boolean ecuIncl;

    @Column(name = "ECU_OPTIONAL")
    private String ecuOptional;

    @Column(name = "ECU_IN")
    private Boolean ecuIn;

    @Column(name = "ECU_FOR")
    private String ecuFor;

    @Column(name = "ECU_AT")
    private String ecuAt;

    @Column(name = "ECU_BASIS")
    private String ecuBasis;

    @Column(name = "ECU_OTHERS")
    private String ecuOthers;

    @Column(name = "POS_OPT")
    private Boolean posOpt;

    @Column(name = "POS_OUT_PATIENT")
    private Boolean posOutPatient;

    @Column(name = "POS_IN_PATIENT")
    private Boolean posInPatient;

    @Column(name = "POS_CONSULT")
    private String posConsult;

    @Column(name = "POS_PROF_FEE")
    private String posProfFee;

    @Column(name = "POS_LAB_EXAM")
    private String posLabExam;

    @Column(name = "POS_HOSP_BILL_DEDUCT")
    private String posHospBillDeduct;

    @Column(name = "ADD_AMBU_PRIC")
    private String addAmbuPric;

    @Column(name = "POS_HOSP_BILL_CO_PAYMENT")
    private String posHospBillCoPayment;

    @Column(name = "ADD_AMBU_PER")
    private String addAmbuPer;

    @Column(name = "ADD_AMBU_BEN")
    private String addAmbuBen;

    @Column(name = "PRE_EMP")
    private String preEmp;

    @Column(name = "PRE_EMP_PRICE")
    private String preEmpPrice;

    @Column(name = "PRE_EMP_EXP_REFUND")
    private String preEmpExpRefund;

    @Column(name = "PRE_EMP_FAST_PER")
    private String preEmpFastPer;

    @Column(name = "PRE_EMP_FAST_PRICE")
    private String preEmpFastPrice;

    @Column(name = "AGENTS_NOTES")
    private String agentsNotes;

    @Column(name = "AGENT_OTHERS")
    private String agentOthers;

    @Column(name = "MP_INCASE")
    private String mpInCase;

    @Column(name = "RSP_CLAUSE")
    private Boolean rspClause;

    @Column(name = "MP_MEDICARE")
    private Boolean mpMedicare;

    @Column(name = "POS_FOR")
    private String posFor;

    @Column(name = "PRINCIPAL_TO")
    private String principalTo;

    @Column(name = "DEP_SPOUSE_TO")
    private String depSpouseTo;

    @Column(name = "DEP_CHILDREN_TO")
    private String depChildrenTo;

    @Column(name = "DEP_PARENTS_TO")
    private String depParentsTo;

    @Column(name = "DEP_BROS_SIS_TO")
    private String depBrosSisTo;

    @Column(name = "DEP_CHILDREN_UNIT")
    private Integer depChildrenUnit;

    @Column(name = "DEP_CHILDREN_TO_UNIT")
    private Integer depChildrenToUnit;

    @Column(name = "DEP_BROS_SIS_UNIT")
    private Integer depBrosSisUnit;

    @Column(name = "DEP_BROS_SIS_TO_UNIT")
    private Integer depBrosSisToUnit;

    @Column(name = "ADD_RATES")
    private String addRates;

    @Column(name = "MP_PHIL")
    private Boolean mpPhil;

    @Column(name = "RSP_NO_NEW")
    private Boolean rspNoNew;

    @Column(name = "PRE_EMP_EXP_PERCENT")
    private String preEmpExpPercent;

    @Column(name = "OPCS_PREPOST_NATAL_PRN")
    private String opcsPrepostNatalPrn;

    @Column(name = "OPCS_PREPOST_NATAL_DEP")
    private String opcsPrepostNatalDep;

    @Column(name = "OPCS_PREPOST_NATAL_EXT")
    private String opcsPrepostNatalExt;

    @Column(name = "MP_Others")
    private String mpOthers;

    @Column(name = "VATExempt")
    private Integer vatExempt;

//    @Column(name = "ZeroRated")
//    private Integer zeroRated;

    @Column(name = "PRE_CASHBASIS")
    private Integer preCashBasis;
}