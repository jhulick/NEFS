
/* this file is generated by com.netspective.sparx.form.Dialog.getSubclassedDialogContextCode(), do not modify (you can extend it, though) */

package auto.dcb.subject;

import com.netspective.sparx.form.*;
import com.netspective.sparx.form.field.*;
import com.netspective.sparx.form.field.type.*;

public class CompleteProfileContext
{
    public static final String DIALOG_ID = "subject.complete-profile";
    private DialogContext dialogContext;
    private DialogContext.DialogFieldStates fieldStates;

    public CompleteProfileContext(DialogContext dc)
    {
        this.dialogContext = dc;
        this.fieldStates = dc.getFieldStates();
    }

    public DialogContext getDialogContext() { return dialogContext; }


	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState getNamePrefixState() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState) fieldStates.getState("name_prefix"); }
	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue getNamePrefix() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue) getNamePrefixState().getValue(); }
	public DialogFieldFlags getNamePrefixStateFlags() { return getNamePrefixState().getStateFlags(); }
	public String getNamePrefixPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.name_prefix"); }
	public String getNamePrefixPublicRequestParam() { return dialogContext.getRequest().getParameter("name_prefix"); }
	public com.netspective.sparx.form.field.type.SelectField getNamePrefixField() { return (com.netspective.sparx.form.field.type.SelectField) getNamePrefixState().getField(); }

	public com.netspective.sparx.form.field.type.TextField.TextFieldState getNameLastState() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState) fieldStates.getState("name_last"); }
	public com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue getNameLast() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue) getNameLastState().getValue(); }
	public DialogFieldFlags getNameLastStateFlags() { return getNameLastState().getStateFlags(); }
	public String getNameLastPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.name_last"); }
	public String getNameLastPublicRequestParam() { return dialogContext.getRequest().getParameter("name_last"); }
	public com.netspective.sparx.form.field.type.TextField getNameLastField() { return (com.netspective.sparx.form.field.type.TextField) getNameLastState().getField(); }

	public com.netspective.sparx.form.field.type.TextField.TextFieldState getNameFirstState() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState) fieldStates.getState("name_first"); }
	public com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue getNameFirst() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue) getNameFirstState().getValue(); }
	public DialogFieldFlags getNameFirstStateFlags() { return getNameFirstState().getStateFlags(); }
	public String getNameFirstPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.name_first"); }
	public String getNameFirstPublicRequestParam() { return dialogContext.getRequest().getParameter("name_first"); }
	public com.netspective.sparx.form.field.type.TextField getNameFirstField() { return (com.netspective.sparx.form.field.type.TextField) getNameFirstState().getField(); }

	public com.netspective.sparx.form.field.type.TextField.TextFieldState getNameMiddleState() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState) fieldStates.getState("name_middle"); }
	public com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue getNameMiddle() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue) getNameMiddleState().getValue(); }
	public DialogFieldFlags getNameMiddleStateFlags() { return getNameMiddleState().getStateFlags(); }
	public String getNameMiddlePrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.name_middle"); }
	public String getNameMiddlePublicRequestParam() { return dialogContext.getRequest().getParameter("name_middle"); }
	public com.netspective.sparx.form.field.type.TextField getNameMiddleField() { return (com.netspective.sparx.form.field.type.TextField) getNameMiddleState().getField(); }

	public com.netspective.sparx.form.field.type.TextField.TextFieldState getNameSuffixState() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState) fieldStates.getState("name_suffix"); }
	public com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue getNameSuffix() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue) getNameSuffixState().getValue(); }
	public DialogFieldFlags getNameSuffixStateFlags() { return getNameSuffixState().getStateFlags(); }
	public String getNameSuffixPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.name_suffix"); }
	public String getNameSuffixPublicRequestParam() { return dialogContext.getRequest().getParameter("name_suffix"); }
	public com.netspective.sparx.form.field.type.TextField getNameSuffixField() { return (com.netspective.sparx.form.field.type.TextField) getNameSuffixState().getField(); }


	public com.netspective.sparx.form.field.type.DateTimeField.DateTimeFieldState getDateOfBirthState() { return (com.netspective.sparx.form.field.type.DateTimeField.DateTimeFieldState) fieldStates.getState("date_of_birth"); }
	public com.netspective.sparx.form.field.type.DateTimeField.DateTimeFieldState.DateTimeFieldValue getDateOfBirth() { return (com.netspective.sparx.form.field.type.DateTimeField.DateTimeFieldState.DateTimeFieldValue) getDateOfBirthState().getValue(); }
	public DialogFieldFlags getDateOfBirthStateFlags() { return getDateOfBirthState().getStateFlags(); }
	public String getDateOfBirthPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.date_of_birth"); }
	public String getDateOfBirthPublicRequestParam() { return dialogContext.getRequest().getParameter("date_of_birth"); }
	public com.netspective.sparx.form.field.type.DateTimeField getDateOfBirthField() { return (com.netspective.sparx.form.field.type.DateTimeField) getDateOfBirthState().getField(); }

	public com.netspective.sparx.form.field.type.SocialSecurityField.State getSsnState() { return (com.netspective.sparx.form.field.type.SocialSecurityField.State) fieldStates.getState("ssn"); }
	public com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue getSsn() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue) getSsnState().getValue(); }
	public DialogFieldFlags getSsnStateFlags() { return getSsnState().getStateFlags(); }
	public String getSsnPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.ssn"); }
	public String getSsnPublicRequestParam() { return dialogContext.getRequest().getParameter("ssn"); }
	public com.netspective.sparx.form.field.type.SocialSecurityField getSsnField() { return (com.netspective.sparx.form.field.type.SocialSecurityField) getSsnState().getField(); }

	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState getGenderIdState() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState) fieldStates.getState("gender_id"); }
	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue getGenderId() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue) getGenderIdState().getValue(); }
	public DialogFieldFlags getGenderIdStateFlags() { return getGenderIdState().getStateFlags(); }
	public String getGenderIdPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.gender_id"); }
	public String getGenderIdPublicRequestParam() { return dialogContext.getRequest().getParameter("gender_id"); }
	public com.netspective.sparx.form.field.type.SelectField getGenderIdField() { return (com.netspective.sparx.form.field.type.SelectField) getGenderIdState().getField(); }

	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState getMaritalStatusIdState() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState) fieldStates.getState("marital_status_id"); }
	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue getMaritalStatusId() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue) getMaritalStatusIdState().getValue(); }
	public DialogFieldFlags getMaritalStatusIdStateFlags() { return getMaritalStatusIdState().getStateFlags(); }
	public String getMaritalStatusIdPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.marital_status_id"); }
	public String getMaritalStatusIdPublicRequestParam() { return dialogContext.getRequest().getParameter("marital_status_id"); }
	public com.netspective.sparx.form.field.type.SelectField getMaritalStatusIdField() { return (com.netspective.sparx.form.field.type.SelectField) getMaritalStatusIdState().getField(); }

	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState getBloodTypeIdState() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState) fieldStates.getState("blood_type_id"); }
	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue getBloodTypeId() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue) getBloodTypeIdState().getValue(); }
	public DialogFieldFlags getBloodTypeIdStateFlags() { return getBloodTypeIdState().getStateFlags(); }
	public String getBloodTypeIdPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.blood_type_id"); }
	public String getBloodTypeIdPublicRequestParam() { return dialogContext.getRequest().getParameter("blood_type_id"); }
	public com.netspective.sparx.form.field.type.SelectField getBloodTypeIdField() { return (com.netspective.sparx.form.field.type.SelectField) getBloodTypeIdState().getField(); }

	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState getEthnicityIdState() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState) fieldStates.getState("ethnicity_id"); }
	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue getEthnicityId() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue) getEthnicityIdState().getValue(); }
	public DialogFieldFlags getEthnicityIdStateFlags() { return getEthnicityIdState().getStateFlags(); }
	public String getEthnicityIdPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.ethnicity_id"); }
	public String getEthnicityIdPublicRequestParam() { return dialogContext.getRequest().getParameter("ethnicity_id"); }
	public com.netspective.sparx.form.field.type.SelectField getEthnicityIdField() { return (com.netspective.sparx.form.field.type.SelectField) getEthnicityIdState().getField(); }

	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState getLanguageIdState() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState) fieldStates.getState("language_id"); }
	public com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue getLanguageId() { return (com.netspective.sparx.form.field.type.SelectField.SelectFieldState.SelectFieldValue) getLanguageIdState().getValue(); }
	public DialogFieldFlags getLanguageIdStateFlags() { return getLanguageIdState().getStateFlags(); }
	public String getLanguageIdPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.language_id"); }
	public String getLanguageIdPublicRequestParam() { return dialogContext.getRequest().getParameter("language_id"); }
	public com.netspective.sparx.form.field.type.SelectField getLanguageIdField() { return (com.netspective.sparx.form.field.type.SelectField) getLanguageIdState().getField(); }


	public com.netspective.sparx.form.field.type.PhoneField.State getWorkPhoneState() { return (com.netspective.sparx.form.field.type.PhoneField.State) fieldStates.getState("work_phone"); }
	public com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue getWorkPhone() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue) getWorkPhoneState().getValue(); }
	public DialogFieldFlags getWorkPhoneStateFlags() { return getWorkPhoneState().getStateFlags(); }
	public String getWorkPhonePrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.work_phone"); }
	public String getWorkPhonePublicRequestParam() { return dialogContext.getRequest().getParameter("work_phone"); }
	public com.netspective.sparx.form.field.type.PhoneField getWorkPhoneField() { return (com.netspective.sparx.form.field.type.PhoneField) getWorkPhoneState().getField(); }

	public com.netspective.sparx.form.field.type.PhoneField.State getHomePhoneState() { return (com.netspective.sparx.form.field.type.PhoneField.State) fieldStates.getState("home_phone"); }
	public com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue getHomePhone() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue) getHomePhoneState().getValue(); }
	public DialogFieldFlags getHomePhoneStateFlags() { return getHomePhoneState().getStateFlags(); }
	public String getHomePhonePrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.home_phone"); }
	public String getHomePhonePublicRequestParam() { return dialogContext.getRequest().getParameter("home_phone"); }
	public com.netspective.sparx.form.field.type.PhoneField getHomePhoneField() { return (com.netspective.sparx.form.field.type.PhoneField) getHomePhoneState().getField(); }

	public com.netspective.sparx.form.field.type.TextField.TextFieldState getEmailState() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState) fieldStates.getState("email"); }
	public com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue getEmail() { return (com.netspective.sparx.form.field.type.TextField.TextFieldState.TextFieldValue) getEmailState().getValue(); }
	public DialogFieldFlags getEmailStateFlags() { return getEmailState().getStateFlags(); }
	public String getEmailPrivateRequestParam() { return dialogContext.getRequest().getParameter("_dc.email"); }
	public String getEmailPublicRequestParam() { return dialogContext.getRequest().getParameter("email"); }
	public com.netspective.sparx.form.field.type.TextField getEmailField() { return (com.netspective.sparx.form.field.type.TextField) getEmailState().getField(); }

}