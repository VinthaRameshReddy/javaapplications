package com.medgo.member.mapper;


import com.medgo.member.domain.entity.membership.MaternityBenefitsEntity;
import com.medgo.member.domain.entity.membership.MembershipEntity;
import com.medgo.member.domain.response.MaternityBenefitsResponse;
import com.medgo.member.domain.response.MembershipResponse;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    public MembershipResponse toMembershipResponse(MembershipEntity member) {
        MembershipResponse response = new MembershipResponse();
        response.setMemberCode(member.getMemberCode());
        response.setPrincipalCode(member.getPrincipalCode());
        response.setMemberAppNum(member.getMemberAppNum());
        response.setFirstName(member.getFirstName());
        response.setLastName(member.getLastName());
        response.setMiddleName(member.getMiddleName());
        response.setSex(member.getSex());
        response.setAge(member.getAge());
        response.setBirthDate(member.getBirthDate());
        response.setCivilStatus(member.getCivilStatus());
        response.setMemStatusCode(member.getMemStatusCode());
        response.setMemStatus(member.getMemStatus());
        response.setMemType(member.getMemType());
        response.setPlanDesc(member.getPlanDesc());
        response.setEffectivityDate(member.getEffectivityDate());
        response.setValidityDate(member.getValidityDate());
        response.setResignDate(member.getResignDate());
        response.setMotherCode(member.getMotherCode());
        response.setAccountCode(member.getAccountCode());
        response.setAccountName(member.getAccountName());
        response.setAccountType(member.getAccountType());
        response.setIdRem(member.getIdRem());
        response.setIdRem2(member.getIdRem2());
        response.setIdRem3(member.getIdRem3());
        response.setIdRem4(member.getIdRem4());
        response.setIdRem5(member.getIdRem5());
        response.setIdRem6(member.getIdRem6());
        response.setIdRem7(member.getIdRem7());
        response.setOtherRemarks(member.getOtherRemarks());
        response.setRspRoomRateId(member.getRspRoomRateId());
        return response;
    }

//    public UserDependentResponse toUserDependentResponse() {
//        UserDependentResponse response = new UserDependentResponse();
//        response.setId(dependent.getId());
//        response.setDependentCode(dependent.getDependentCode());
//        response.setStatus(dependent.getStatus());
//        return response;
//    }

    public MaternityBenefitsResponse toMaternityBenefitsResponse(MaternityBenefitsEntity maternityBenefits) {
        MaternityBenefitsResponse response = new MaternityBenefitsResponse();
        response.setPreForBilling(maternityBenefits.getPreForBilling());
        response.setNoEmp(maternityBenefits.getNoEmp());
        response.setNoEmpDep(maternityBenefits.getNoEmpDep());
        response.setEligStd(maternityBenefits.getEligStd());
        response.setPrincipal(maternityBenefits.getPrincipal());
        response.setDepSpouse(maternityBenefits.getDepSpouse());
        response.setDepChildren(maternityBenefits.getDepChildren());
        response.setDepParents(maternityBenefits.getDepParents());
        response.setDepBrosSis(maternityBenefits.getDepBrosSis());
        response.setDepNote(maternityBenefits.getDepNote());
        response.setMpStgItg(maternityBenefits.getMpStgItg());
        response.setMpExclusive(maternityBenefits.getMpExclusive());
        response.setMpRmBoard(maternityBenefits.getMpRmBoard());
        response.setMpAuto(maternityBenefits.getMpAuto());
        response.setMabFor(maternityBenefits.getMabFor());
        response.setMabIncPremium(maternityBenefits.getMabIncPremium());
        response.setMabOptional(maternityBenefits.getMabOptional());
        response.setMabOptPayable(maternityBenefits.getMabOptPayable());
        response.setMabNoMarriedFml(maternityBenefits.getMabNoMarriedFml());
        response.setMabNoMarriedMl(maternityBenefits.getMabNoMarriedMl());
        response.setMabNormDelivery(maternityBenefits.getMabNormDelivery());
        response.setMabCaearean(maternityBenefits.getMabCaearean());
        response.setMabAbort(maternityBenefits.getMabAbort());
        response.setMabHomeDelivery(maternityBenefits.getMabHomeDelivery());
        response.setMabAbnormal(maternityBenefits.getMabAbnormal());
        response.setMabOthers(maternityBenefits.getMabOthers());
        response.setEcuOut(maternityBenefits.getEcuOut());
        response.setEcuIncl(maternityBenefits.getEcuIncl());
        response.setEcuOptional(maternityBenefits.getEcuOptional());
        response.setEcuIn(maternityBenefits.getEcuIn());
        response.setEcuFor(maternityBenefits.getEcuFor());
        response.setEcuAt(maternityBenefits.getEcuAt());
        response.setEcuBasis(maternityBenefits.getEcuBasis());
        response.setEcuOthers(maternityBenefits.getEcuOthers());
        response.setPosOpt(maternityBenefits.getPosOpt());
        response.setPosOutPatient(maternityBenefits.getPosOutPatient());
        response.setPosInPatient(maternityBenefits.getPosInPatient());
        response.setPosConsult(maternityBenefits.getPosConsult());
        response.setPosProfFee(maternityBenefits.getPosProfFee());
        response.setPosLabExam(maternityBenefits.getPosLabExam());
        response.setPosHospBillDeduct(maternityBenefits.getPosHospBillDeduct());
        response.setAddAmbuPric(maternityBenefits.getAddAmbuPric());
        response.setPosHospBillCoPayment(maternityBenefits.getPosHospBillCoPayment());
        response.setAddAmbuPer(maternityBenefits.getAddAmbuPer());
        response.setAddAmbuBen(maternityBenefits.getAddAmbuBen());
        response.setPreEmp(maternityBenefits.getPreEmp());
        response.setPreEmpPrice(maternityBenefits.getPreEmpPrice());
        response.setPreEmpExpRefund(maternityBenefits.getPreEmpExpRefund());
        response.setPreEmpFastPer(maternityBenefits.getPreEmpFastPer());
        response.setPreEmpFastPrice(maternityBenefits.getPreEmpFastPrice());
        response.setAgentsNotes(maternityBenefits.getAgentsNotes());
        response.setAgentOthers(maternityBenefits.getAgentOthers());
        response.setMpInCase(maternityBenefits.getMpInCase());
        response.setRspClause(maternityBenefits.getRspClause());
        response.setMpMedicare(maternityBenefits.getMpMedicare());
        response.setPosFor(maternityBenefits.getPosFor());
        response.setPrincipalTo(maternityBenefits.getPrincipalTo());
        response.setDepSpouseTo(maternityBenefits.getDepSpouseTo());
        response.setDepChildrenTo(maternityBenefits.getDepChildrenTo());
        response.setDepParentsTo(maternityBenefits.getDepParentsTo());
        response.setDepBrosSisTo(maternityBenefits.getDepBrosSisTo());
        response.setDepChildrenUnit(maternityBenefits.getDepChildrenUnit());
        response.setDepChildrenToUnit(maternityBenefits.getDepChildrenToUnit());
        response.setDepBrosSisUnit(maternityBenefits.getDepBrosSisUnit());
        response.setDepBrosSisToUnit(maternityBenefits.getDepBrosSisToUnit());
        response.setAddRates(maternityBenefits.getAddRates());
        response.setMpPhil(maternityBenefits.getMpPhil());
        response.setRspNoNew(maternityBenefits.getRspNoNew());
        response.setPreEmpExpPercent(maternityBenefits.getPreEmpExpPercent());
        response.setOpcsPrepostNatalPrn(maternityBenefits.getOpcsPrepostNatalPrn());
        response.setOpcsPrepostNatalDep(maternityBenefits.getOpcsPrepostNatalDep());
        response.setOpcsPrepostNatalExt(maternityBenefits.getOpcsPrepostNatalExt());
        response.setMpOthers(maternityBenefits.getMpOthers());
        response.setVatExempt(maternityBenefits.getVatExempt());
      //  response.setZeroRated(maternityBenefits.getZeroRated());
        response.setPreCashBasis(maternityBenefits.getPreCashBasis());
        return response;
    }
}