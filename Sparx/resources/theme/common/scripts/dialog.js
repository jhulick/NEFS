/*
 * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
 *
 * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
 * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
 * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
 * before using The Software. Any use of The Software indicates agreement with the NSL.
 *
 * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
 *
 * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
 *    containing the .class files) and only as part of an application that uses The Software as part of its primary
 *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
 *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
 *    same restrictions.
 *
 * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
 *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
 *
 * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
 *    used to endorse or appear in products derived from The Software without written consent of Netspective.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
 * ARE HEREBY DISCLAIMED.
 *
 * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
 * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
 * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */

var DIALOGFIELD_PREFIX = '_dc';
var FIELDROW_PREFIX = "_dfr.";
var GRIDFIELDROW_PREFIX = "_dgfr.";
var GRIDHEADROW_PREFIX = "_dghr.";
var FIELDNAME_IGNORE_VALIDATION = DIALOGFIELD_PREFIX + ".ignore_val";
var FIELDNAME_VALIDATE_TRIGGER_FIELD = "validate_trigger_field";

var ALLOW_CLIENT_VALIDATION            = true;
var TRANSLATE_ENTER_KEY_TO_TAB_KEY     = false;
var ENABLE_KEYPRESS_FILTERS            = true;
var SHOW_DATA_CHANGED_MESSAGE_ON_LEAVE = false;
var HIDE_HINTS_UNTIL_FOCUS             = false;
var CLEAR_TEXT_ON_VALIDATE_ERROR       = false;

var anyControlChangedEventCalled = false;
var submittedDialogValid = false;
var cancelBubbleOnError = true;

function setAllowValidation(value)
{
	ALLOW_CLIENT_VALIDATION = value;
}

// Get the dialog field control for IE4
function getControl_IE4(dialog, id)
{
	return document.all.item(id);
}

// Get the dialog field control for DOM browsers such as IE5, IE6 and NS6
function getControl_Dom(dialog, id)
{
	if (id.substring(0,3) == DIALOGFIELD_PREFIX)
		return document.getElementById(dialog.name).elements[id];
	else
		return document.getElementById(id);
}

// Get the dialog field control for Netscape 4
function getControl_NS4(dialog, id)
{
	// a dialog field because the ID starts with a PREFIX
	if (id.substring(0,3) == DIALOGFIELD_PREFIX)
		return document.forms[dialog.name].elements[id];
}

// based on which browser is currently running, get the control using the appropriate function
function getControl(dialog, id)
{
	if (browser.ie5 || browser.ie6 || browser.ie7 || browser.ns6)
	{
		return getControl_Dom(dialog, id);
	}
	else if (browser.ns4)
	{
		return getControl_NS4(dialog, id);
	}
	else if (browser.ie4)
	{
		return getControl_IE4(dialog, id);
	}
}

function radioButtonSelected(fieldName, value)
{
	// radio buttons are named {fieldName}{value} like "_dc.fieldName0" and "_dc.fieldName1", etc
	var fieldId = DIALOGFIELD_PREFIX + "." + fieldName + value;
	var control = getControl(activeDialog, fieldId);
	if(control == null)
	{
		alert("Field '" + fieldId + "' not found in active dialog -- can't check for radio button value");
		return false;
	}

	return control.checked;
}

//****************************************************************************
// ClientDataEntrypion type
//****************************************************************************

function ClientDataEncryption(type, salt)
{
	this.type = type;
	this.salt = salt;
	this.getEncryptedValue = ClientDataEncryption_getEncryptedValue;
}

function ClientDataEncryption_getEncryptedValue(value)
{
    if(this.type == 'unix-crypt')
    {
        var result = Javacrypt.crypt(this.salt, value);
        return result[0];
    }
    else
    {
        alert("Unknown encryption type:  " + this.type);
    }
}

//****************************************************************************
// FieldType class
//****************************************************************************

function FieldType(name, onFinalizeDefn, onValidate, onChange, onFocus, onBlur, onKeyPress, onClick)
{
	this.type = name;
	this.finalizeDefn = onFinalizeDefn;
	this.isValid = onValidate;
	this.getFocus = onFocus;
	this.valueChanged = onChange;
	this.keyPress = onKeyPress;
	this.loseFocus = onBlur;
	this.click = onClick;
}

var FIELD_TYPES = new Array();

function addFieldType(name, onFinalizeDefn, onValidate, onChange, onFocus, onBlur, onKeyPress, onClick)
{
	FIELD_TYPES[name] = new FieldType(name, onFinalizeDefn, onValidate, onChange, onFocus, onBlur, onKeyPress, onClick);
}

//****************************************************************************
// Dialog class
//****************************************************************************

function Dialog(name)
{
	this.name = name;
	this.fields = new Array();              // straight list (simple array)
	this.fieldsById = new Array();          // hash -- value is field
	this.fieldsByQualName = new Array();    // hash -- value is field

	// the remaining are object-based methods
	this.registerField = Dialog_registerField;
	this.finalizeContents = Dialog_finalizeContents;
	this.isValid = Dialog_isValid;
	this.getFieldControl = Dialog_getFieldControl;
	this.allowValidation = Dialog_allowValidation;
	this.hideHintsUntilFocus = Dialog_hideHintsUntilFocus;
}

function Dialog_registerField(field)
{
	field.fieldIndex = this.fields.length;
	this.fields[field.fieldIndex] = field;
	this.fieldsById[field.controlId] = field;
	this.fieldsByQualName[field.qualifiedName] = field;

	if(field.fieldIndex > 0)
		field.prevFieldIndex = field.fieldIndex-1;
	field.nextFieldIndex = field.fieldIndex+1;
}

function Dialog_finalizeContents()
{
	var dialogFields = this.fields;
	for(var i = 0; i < dialogFields.length; i++)
		dialogFields[i].finalizeContents(this);
}

function Dialog_allowValidation()
{
	return ALLOW_CLIENT_VALIDATION;
}

function Dialog_hideHintsUntilFocus()
{
	return HIDE_HINTS_UNTIL_FOCUS;
}

function Dialog_isValid()
{
	var dialogFields = this.fields;
	for(var i = 0; i < dialogFields.length; i++)
	{
		var field = dialogFields[i];
		if(field.requiresPreSubmit)
			field.doPreSubmit();
	}

	if(! this.allowValidation())
		return true;

	var isValid = true;
	for(var i = 0; i < dialogFields.length; i++)
	{
		var field = dialogFields[i];
		if(! field.isValid())
		{
			isValid = false;
			break;
		}
	}

	if (isValid)
	{
	    submittedDialogValid = true;
        for(var i = 0; i < dialogFields.length; i++)
        {
            var field = dialogFields[i];
            if(field.encryption != null)
            {
                var control = field.getControl(this);
                control.value = field.encryption.getEncryptedValue(control.value);
            }
        }
	}
	return isValid;
}

function Dialog_getFieldControl(qualifiedName)
{
	var field = this.fieldsByQualName[qualifiedName];
	if(field != null)
		return field.getControl(this);
	else
		return null;
}

var activeDialog = null;

function setActiveDialog(dialog)
{
	activeDialog = dialog;
}

//****************************************************************************
// DialogField class
//****************************************************************************

// These constants MUST be kept identical to what is in com.netspective.sparx.form.DialogField

var FLDFLAG_REQUIRED                           = 1;
var FLDFLAG_PRIMARYKEY                         = FLDFLAG_REQUIRED * 2;
var FLDFLAG_PRIMARYKEY_GENERATED               = FLDFLAG_PRIMARYKEY * 2;
var FLDFLAG_INVISIBLE                          = FLDFLAG_PRIMARYKEY_GENERATED * 2;
var FLDFLAG_READONLY                           = FLDFLAG_INVISIBLE * 2;
var FLDFLAG_INITIAL_FOCUS                      = FLDFLAG_READONLY * 2;
var FLDFLAG_PERSIST                            = FLDFLAG_INITIAL_FOCUS * 2;
var FLDFLAG_CREATEADJACENTAREA                 = FLDFLAG_PERSIST * 2;
var FLDFLAG_SHOWCAPTIONASCHILD                 = FLDFLAG_CREATEADJACENTAREA * 2;
var FLDFLAG_INPUT_HIDDEN                       = FLDFLAG_SHOWCAPTIONASCHILD * 2;
var FLDFLAG_HAS_CONDITIONAL_DATA               = FLDFLAG_INPUT_HIDDEN * 2;
var FLDFLAG_COLUMN_BREAK_BEFORE                = FLDFLAG_HAS_CONDITIONAL_DATA * 2;
var FLDFLAG_COLUMN_BREAK_AFTER                 = FLDFLAG_COLUMN_BREAK_BEFORE * 2;
var FLDFLAG_BROWSER_READONLY                   = FLDFLAG_COLUMN_BREAK_AFTER * 2;
var FLDFLAG_IDENTIFIER                         = FLDFLAG_BROWSER_READONLY * 2;
var FLDFLAG_READONLY_HIDDEN_UNLESS_HAS_DATA    = FLDFLAG_IDENTIFIER * 2;
var FLDFLAG_READONLY_INVISIBLE_UNLESS_HAS_DATA = FLDFLAG_READONLY_HIDDEN_UNLESS_HAS_DATA * 2;
var FLDFLAG_DOUBLEENTRY                        = FLDFLAG_READONLY_INVISIBLE_UNLESS_HAS_DATA * 2;
var FLDFLAG_SCANNABLE                          = FLDFLAG_DOUBLEENTRY * 2;
var FLDFLAG_AUTOBLUR                           = FLDFLAG_SCANNABLE * 2;
var FLDFLAG_SUBMIT_ONBLUR                      = FLDFLAG_AUTOBLUR * 2;
var FLDFLAG_CREATEADJACENTAREA_HIDDEN          = FLDFLAG_SUBMIT_ONBLUR * 2;
var FLDFLAG_STARTCUSTOM                        = FLDFLAG_CREATEADJACENTAREA_HIDDEN * 2; // all DialogField "children" will use this

// These constants MUST be kept identical to what is in com.netspective.sparx.form.field.SelectField
var SELECTSTYLE_RADIO        = 0;
var SELECTSTYLE_COMBO        = 1;
var SELECTSTYLE_LIST         = 2;
var SELECTSTYLE_MULTICHECK   = 3;
var SELECTSTYLE_MULTILIST    = 4;
var SELECTSTYLE_MULTIDUAL    = 5;
var SELECTSTYLE_POPUP        = 6;
var SELECTSTYLE_TEXT         = 7;
var SELECTSTYLE_AUTOCOMPLETE = 8;
var SELECTSTYLE_MULTIAUTOCOMPLETE = 9;

var DATE_DTTYPE_DATEONLY = 0;
var DATE_DTTYPE_TIMEONLY = 1;
var DATE_DTTYPE_BOTH     = 2;
var DATE_DTTYPE_MONTH_YEAR_ONLY = 3;

function DialogField(type, id, name, qualifiedName, caption, flags)
{
	this.typeName = type;
	this.type = FIELD_TYPES[type];
	if (typeof this.type == "undefined")
		this.type = null;
	this.controlId = id;
	this.name = name;
	this.qualifiedName = qualifiedName;
	this.caption = caption;
	this.customHandlers = new FieldType("Custom", null, null, null, null, null, null, null);
	this.flags = flags;
    this.dependentConditions = new Array();
	this.style = null;
	this.requiresPreSubmit = false;
	this.currentlyVisible = true;
	this.encryption = null;

    this.fieldIndex = -1;
	this.prevFieldIndex = -1;
	this.nextFieldIndex = -1;

	// the remaining are object-based methods
	if (browser.ie5 || browser.ie6 || browser.ie7 || browser.ns6)
	{
		this.getControl = DialogField_getControl_Dom;
		this.getControlByQualifiedName = DialogField_getControlByQualifiedName_Dom;
		this.getFieldAreaElem = DialogField_getFieldAreaElem_Dom;
	}
	else if (browser.ns4)
	{
		this.getControl = DialogField_getControl_NS4;
		this.getControlByQualifiedName = DialogField_getControlByQualifiedName_NS4;
		this.getFieldAreaElem = DialogField_getFieldAreaElem_NS4;
	}
	else if (browser.ie4)
	{
		this.getControl = DialogField_getControl_IE4;
		this.getControlByQualifiedName = DialogField_getControlByQualifiedName_IE4;
		this.getFieldAreaElem = DialogField_getFieldAreaElem_IE4;
	}

	this.getAdjacentArea = DialogField_getAdjacentArea;
	this.evaluateConditionals = DialogField_evaluateConditionals;
	this.finalizeContents = DialogField_finalizeContents;
	this.isValid = DialogField_isValid;
	this.doPreSubmit = DialogField_doPreSubmit;
	this.focusNext = DialogField_focusNext;
	this.alertRequired = DialogField_alertRequired;
	this.isRequired = DialogField_isRequired;
	this.isReadOnly = DialogField_isReadOnly;
	this.alertMessage = DialogField_alertMessage;
	this.isVisible = DialogField_isVisible;
	this.setVisible = DialogField_setVisible;
	this.setRequired = DialogField_setRequired;
	this.setValue = DialogField_setValue;
	this.setReadOnly = DialogField_setReadOnly;
}

function DialogField_setValue(value)
{
    var control = this.getControl(dialog);
    control.value = value;
}

function DialogField_isRequired()
{
	return (this.flags & FLDFLAG_REQUIRED) != 0 && this.isVisible();
}

function getElementStyle(elemID, IEStyleProp, CSSStyleProp)
{
    var elem = document.getElementById(elemID);
    if (elem.currentStyle)
    {
        return elem.currentStyle[IEStyleProp];
    }
    else if (window.getComputedStyle)
    {
        var compStyle = window.getComputedStyle(elem, "");
        return compStyle.getPropertyValue(CSSStyleProp);
    }
    return "";
}

/**
 * Changes the label and input appearance based on the field's REQUIRED status
 */
function DialogField_setRequired(required)
{
    if (required)
    {
        this.flags = this.flags | FLDFLAG_REQUIRED;
        var objLabels = document.getElementsByTagName("LABEL");
        var control = this.getControl(dialog);
        for (var i = 0; i < objLabels.length; i++)
        {
            if (objLabels[i].htmlFor == this.controlId)
            {
                //  NOTE: This is dependent on how the skin looks!
                objLabels[i].className = 'dialog-field-caption-required';
                control.className = 'dialog-field-input-required';
                break;
            }
        }
    }
    else
    {
        this.flags = this.flags & ~FLDFLAG_REQUIRED; //flags &= ~flag;
        var objLabels = document.getElementsByTagName("LABEL");
        var control = this.getControl(dialog);
        for (var i = 0; i < objLabels.length; i++)
        {
            if (objLabels[i].htmlFor == this.controlId)
            {
                //  NOTE: This is dependent on how the skin looks!
                objLabels[i].className = 'dialog-field-caption';
                control.className = 'dialog-field-input';
                break;
            }
        }
    }
}

function DialogField_setReadOnly(readOnly)
{
    var control = this.getControl(dialog);
    if (readOnly)
    {
        this.flags = this.flags | FLDFLAG_BROWSER_READONLY;
        control.className= 'dialog-field-input-readonly';
        control.readOnly = true;
    }
    else
    {
        this.flags = this.flags & ~FLDFLAG_BROWSER_READONLY;
        control.className= 'dialog-field-input';
        control.readOnly = false;
    }
}


function DialogField_isReadOnly()
{
	return ((this.flags & FLDFLAG_READONLY) != 0) || ((this.flags & FLDFLAG_BROWSER_READONLY) != 0);
}

function DialogField_getAdjacentArea(dialog)
{
	if((this.flags & FLDFLAG_CREATEADJACENTAREA) != 0 )
	{
		return getControl(dialog, this.qualifiedName + "_adjacent");
	}
	else if((this.flags & FLDFLAG_CREATEADJACENTAREA_HIDDEN) != 0 )
	{
		return getControl(dialog, this.qualifiedName + "_adjacent");
	}
	else
		return null;
}

/**
 * Get the dialog field control  using its ID for IE4
 */
function DialogField_getControl_IE4(dialog)
{
	return getControl_IE4(dialog, this.controlId);
}

/**
 * Get the dialog field control  using its ID for DOM browsers such as IE5, IE6 and NS6
 */
function DialogField_getControl_Dom(dialog)
{
	return getControl_Dom(dialog, this.controlId);
}

/**
 * Get the dialog field control  using its ID for Netscape 4
 */
function DialogField_getControl_NS4(dialog)
{
	return getControl_NS4(dialog, this.controlId);
}

/**
 * Get the dialog field control  using its qualified name for IE4
 */
function DialogField_getControlByQualifiedName_IE4(dialog)
{
	return getControl_IE4(dialog, this.qualifiedName);
}

/**
 * Get the dialog field control  using its qualified name for DOM browsers such as IE5, IE6 and NS6
 */
function DialogField_getControlByQualifiedName_Dom(dialog)
{
	return getControl_Dom(dialog, this.qualifiedName);
}

/**
 * Get the dialog field control using its qualified name for Netscape 4
 */
function DialogField_getControlByQualifiedName_NS4(dialog)
{
	return getControl_NS4(dialog, this.qualifiedName);
}

function DialogField_finalizeContents(dialog)
{
	if(this.type != null)
	{
		if(this.type.finalizeDefn != null)
			this.type.finalizeDefn(dialog, this);
	}

	if(this.style != null && this.style == SELECTSTYLE_MULTIDUAL)
		this.requiresPreSubmit = true;

    if(this.dependentConditions.length > 0)
		this.evaluateConditionals(dialog);

	if((this.flags & FLDFLAG_INITIAL_FOCUS) != 0)
	{
		var field = dialog.fieldsByQualName[this.qualifiedName];

		var control = this.getControl(dialog);
		if(control == null)
			alert("Unable to find control '"+this.controlId+"' in DialogField.finalizeContents() -- trying to set initial focus");
		else
		{
			if(browser.ie5 || browser.ie6 || browser.ie7)
			{
				if (control.isContentEditable && field.isVisible())
					control.focus();
			}
			else
			{
				if (field.isVisible())
					control.focus();
			}
		}
	}

    // we only support "extends" for finalizeDefn, not "override"
    if(this.customHandlers.finalizeDefn)
        this.customHandlers.finalizeDefn(dialog, this);
}

function DialogField_evaluateConditionals(dialog)
{
	if(this.isReadOnly())
		return;

	var control = this.getControl(dialog);
	if(control == null)
	{
	    // TODO: Need to add this back in later
		alert("Unable to find control '"+this.controlId+"' in DialogField.evaluateConditionals()");
		return;
	}

	var conditionalFields = this.dependentConditions;
	for(var i = 0; i < conditionalFields.length; i++)
		conditionalFields[i].evaluate(dialog, control);
}

function DialogField_alertMessage(control, message)
{
	if (this.caption == "null")
	{
		alert(message);
	}
	else
	{
		alert(this.caption + ":   " + message);
	}

	if (CLEAR_TEXT_ON_VALIDATE_ERROR)
	{
	    control.value = "";
	}
	control.focus();
	handleCancelBubble(control);
}

function handleCancelBubble(control)
{
	if (cancelBubbleOnError)
	{
		var field = activeDialog.fieldsById[control.name];
		if (field != null && field.doubleEntry == "yes")
		{
			field.firstEntryValue = "";
		}
	}
}

function DialogField_alertRequired(control)
{
	if (this.caption == "null")
	{
		alert("This field is required.");
	}
	else
	{
  	  alert(this.caption + " is required.");
	}
	if(control != null) control.focus();
}

function DialogField_isValid()
{
	if(this.isReadOnly())
		return true;

	// perform default validation first
	var control = this.getControl(dialog);
	if (control == null)
		return true;

	// now see if there are any type-specific validations to perform
	var fieldType = this.type;
	if(fieldType != null && fieldType.isValid != null)
	{
		if (this.customHandlers.isValid != null)
		{
			var valid = true;
			if (this.customHandlers.isValidType == 'extends')
				valid = fieldType.isValid(this, control);
			if (valid)
			{
				valid = this.customHandlers.isValid(this, control);
			}
			return valid;
		}
		else
		{
			return fieldType.isValid(this, control);
		}
	}

	// no type-specific validation found so try and do a generic one
	if(this.isRequired())
	{
		if(eval("typeof control.value") != "undefined")
		{
			if(control.value.length == 0)
			{
				this.alertRequired(control);
				return false;
			}
		}
	}

	return true;
}

function DialogField_doPreSubmit()
{
	if(this.style != null && this.style == SELECTSTYLE_MULTIDUAL)
	{
		// Select all items in multidual elements. If items aren't selected,
		// they won't be posted.
		var control = this.getControl(dialog);
		// It's possible this control may not be rendered as a SELECT element
		// even though the field type was set to SELECT.
		// Depending on the browser, the evaluation of control.options.length could
		// generate a run-time error.
		if ( typeof(control) == "undefined" || control.options == null )
		    return;
		for (var i = 0; i < control.options.length; i++)
		{
			control.options[i].selected = true;
		}
	}
}

function DialogField_focusNext(dialog)
{
	var dialogFieldsCount = dialog.fields.length;
	var nextField = null;
	var nextFieldControl = null;
	var fieldIndex = this.nextFieldIndex;
	var foundEditable = false;
	while((! foundEditable) && fieldIndex < dialogFieldsCount)
	{
			nextField = dialog.fields[fieldIndex];
			nextFieldAreaElem = nextField.getFieldAreaElem(dialog);
			nextFieldControl = nextField.getControl(dialog);

			//nextFieldControl = document.all.item(nextField.controlId);
			if(nextFieldControl != null && nextFieldControl.length > 0)
					nextFieldControl = nextFieldControl[0];

			if(nextField.typeName == "com.netspective.sparx.form.field.type.DialogDirector")
			{
				document.forms[0].form_submit.focus();
			}

			if( (nextFieldControl != null && nextFieldControl.style.display == 'none') ||
				(nextFieldAreaElem != null && nextFieldAreaElem.style.display == 'none') ||
				nextField.typeName == "com.netspective.sparx.form.field.type.field.SeparatorField" ||
				nextField.typeName == "com.netspective.sparx.form.field.type.field.StaticField" ||
				nextField.typeName == "com.netspective.sparx.form.field.type.field.DurationField" || // duration is a composite
				nextField.typeName == "com.netspective.sparx.form.field.type.DialogField" || // composites are of this type
				nextField.typeName == "com.netspective.sparx.form.field.type.DialogDirector" ||
				nextField.typeName == "com.netspective.sparx.form.field.type.field.HtmlField" ||
				nextField.typeName == "com.netspective.sparx.form.field.type.field.SelectField" ||
				(nextField.flags & FLDFLAG_INVISIBLE) != 0 ||
				(nextField.flags & FLDFLAG_READONLY) != 0 ||
				(nextField.flags & FLDFLAG_INPUT_HIDDEN) != 0)
				fieldIndex++;
			else
			 foundEditable = true;
	}

	if(foundEditable)
	{
			if(nextFieldControl != null)
			{
				nextFieldControl.focus();
			}
			else
			{
				alert("No control found for '"+ nextField.controlId + "' (field " + this.nextFieldIndex + ") ["+ nextField.typeName +"]")
			}
			return true;
	}
	else
	{
			document.forms[0].form_submit.focus();
		}

	return false;
}

/**
 * Gets the control of the table row where the dialog field belongs to for IE 4
 * This does not get the control of the dialog field(INPUT)!
 */
function DialogField_getFieldAreaElem_IE4(dialog)
{
	var fieldAreaId = FIELDROW_PREFIX + this.name;
	var fieldAreaElem = getControl_IE4(dialog, fieldAreaId);
	if(fieldAreaElem == null || (typeof fieldAreaElem == "undefined"))
	{
		fieldAreaId = GRIDFIELDROW_PREFIX + this.qualifiedName;
		fieldAreaElem = getControl_IE4(dialog, fieldAreaId);
	}
	return fieldAreaElem;
}

/**
 * Gets the control of the table row where the dialog field belongs to for Dom Browsers.
 * This does not get the control of the dialog field(INPUT)!
 */
function DialogField_getFieldAreaElem_Dom(dialog)
{
	var fieldAreaId = FIELDROW_PREFIX + this.qualifiedName;
	var fieldAreaElem = getControl_Dom(dialog, fieldAreaId);
	if(fieldAreaElem == null || (typeof fieldAreaElem == "undefined"))
	{
		fieldAreaId = GRIDFIELDROW_PREFIX + this.qualifiedName;
		fieldAreaElem = getControl_Dom(dialog, fieldAreaId);
	}
	return fieldAreaElem;
}

/**
 * Gets the control of the table row which the dialog field belongsd to for Netscape 4
 * This does not get the control of the dialog field(INPUT)!
 */
function DialogField_getFieldAreaElem_NS4(dialog)
{
	var fieldAreaId = FIELDROW_PREFIX + this.name;
	var fieldAreaElem = getControl_NS4(dialog, fieldAreaId);
	if(fieldAreaElem == null || (typeof fieldAreaElem == "undefined"))
	{
		fieldAreaId = GRIDFIELDROW_PREFIX + this.qualifiedName;
		fieldAreaElem = getControl_NS4(dialog, fieldAreaId);
	}
	return fieldAreaElem;
}

function DialogField_isVisible()
{
    return this.currentlyVisible == true;
}

function DialogField_setVisible(dialog, visible)
{
    this.currentlyVisible = visible;
    this.evaluateConditionals(dialog);

    // now find the children and hide them too
	var dialogFields = dialog.fields;
	var regExp = new RegExp("^" + field.qualifiedName + '\\.');

	for(var i=0; i<dialogFields.length; i++)
	{
		if(regExp.test(dialogFields[i].qualifiedName))
			dialogFields[i].setVisible(visible);
	}
}

function setAllCheckboxes(sourceCheckbox, otherCheckboxesPrefix)
{
	var isChecked = sourceCheckbox.checked;

	for(var f = 0; f < document.forms.length; f++)
	{
		var form = document.forms[f];
		var elements = form.elements;
		for(var i = 0; i < elements.length; i++)
		{
			control = form.elements[i];
			if(control.name.indexOf(otherCheckboxesPrefix) == 0)
				control.checked = isChecked;
		}
	}
}

function DialogFieldConditionalFlag(source, partner, expression, flag, applyFlag)
{
	this.source = source;
	this.partner = partner;
	this.expression = expression;
    this.flag = flag;
    this.applyFlag = applyFlag; // if applyFlag is true, then the flag is set. If applyFlag is false, then the flag is cleared.
	// the remaining are object-based methods
	this.evaluate = DialogFieldConditionalFlag_evaluate;
}

function DialogFieldConditionalFlag_evaluate(dialog, control)
{
    if(control == null)
    {
        alert("control is null in DialogFieldConditionalFlag.evaluate(dialog, control)");
        return;
    }
    var condSource = dialog.fieldsByQualName[this.source];
    if(eval(this.expression) == true)
	{
	    // set the flag on the dialog
	    if ((this.flag & FLDFLAG_REQUIRED) != 0)
	    {
	        if (this.applyFlag == true)
    		    condSource.setRequired(true);
    		else
    		    condSource.setRequired(false);
        }
        if ((this.flag & FLDFLAG_BROWSER_READONLY) != 0)
        {
            if (this.applyFlag == true)
                condSource.setReadOnly(true);
            else
                condSource.setReadOnly(false);
        }

	}
	else
	{
	    // the evaluation condition was false
	    if ((this.flag & FLDFLAG_REQUIRED) != 0)
	    {
    		condSource.setRequired(false);
    		if (this.applyFlag == true)
    		    condSource.setRequired(false);
    		else
    		    condSource.setRequired(true);
        }
        if ((this.flag & FLDFLAG_BROWSER_READONLY) != 0)
        {
            if (this.applyFlag == true)
                condSource.setReadOnly(false);
            else
                condSource.setReadOnly(true);
        }
	}
}

function DialogFieldConditionalClear(source, partner, expression)
{
	this.source = source;
	this.partner = partner;
	this.expression = expression;

	// the remaining are object-based methods
	this.evaluate = DialogFieldConditionalClear_evaluate;
}

function DialogFieldConditionalClear_evaluate(dialog, control)
{
	if(control == null)
	{
		alert("control is null in DialogFieldConditionalClear.evaluate(control)");
		return;
	}

	var condSource = dialog.fieldsByQualName[this.source];
	if(eval(this.expression) == true)
	{
        condSource.setValue('');
	}
}

//****************************************************************************
// DialogFieldConditionalDisplay class
//****************************************************************************

function DialogFieldConditionalDisplay(source, partner, expression)
{
	this.source = source;
	this.partner = partner;
	this.expression = expression;

	// the remaining are object-based methods
	this.evaluate = DialogFieldConditionalDisplay_evaluate;
}

function DialogFieldConditionalDisplay_evaluate(dialog, control)
{
	// first find the field area that we need to hide/show
	// -- if an ID with the entire field row is found (a primary field)
	//    then go ahead and use that
	// -- if no primary field row is found, find the actual control and
	//    use that to hide/show

	if(control == null)
	{
		alert("control is null in DialogFieldConditionalDisplay.evaluate(control)");
		return;
	}

	var condSource = dialog.fieldsByQualName[this.source];
	var fieldAreaElem = condSource.getFieldAreaElem(dialog);
	if(fieldAreaElem == null || (typeof fieldAreaElem == "undefined"))
	{
		fieldAreaElem = condSource.getControl(dialog);
		if(fieldAreaElem == null || (typeof fieldAreaElem == "undefined"))
		{
			alert ('Neither source element "' + fieldAreaId + '" or "'+ condSource.controlId +'" found in conditional partner.');
			return;
		}
	}

	// now that we have the fieldArea that we want to show/hide go ahead
	// and evaluate the js expression to see if the field should be shown
	// or hidden. remember, the expression is evaluted in the current context
	// which means the word "control" refers to the control that is the
	// the conditional "partner" (not the source)
	if(eval(this.expression) == true)
	{
		condSource.setVisible(dialog, true);
		//fieldAreaElem.className = 'section_field_area_conditional_expanded';
		if (fieldAreaElem.style)
			fieldAreaElem.style.display = '';
		else
			fieldAreaElem.visibility = 'show';
	}
	else
	{
		condSource.setVisible(dialog, false);
		//fieldAreaElem.className = 'section_field_area_conditional';
		if (fieldAreaElem.style)
			fieldAreaElem.style.display = 'none';
		else
			fieldAreaElem.visibility = 'hide';
	}
}


function evaluateQuestions(control, field)
{
	var listString = control.value;
	// Split string at the comma
	var questionList = listString.split(",");
	// Begin loop through the querystring
	for(var i = 0; i < questionList.length; i++)
	{
		if (questionList[i] == field.source)
		return true;
	}
	return false;
}

//****************************************************************************
// Event handlers
//****************************************************************************

function controlOnClick(control, event)
{
	if(control.name == FIELDNAME_IGNORE_VALIDATION)
		setAllowValidation(false);

	field = activeDialog.fieldsById[control.name];
	if(typeof field == "undefined" || field == null || field.type == null) return;

	if (field.customHandlers.click != null)
	{
		var retval = true;
		if (field.customHandlers.clickType == 'extends')
		{
			if (field.type.click != null)
				retval = field.type.click(field, control);
		}
		if (retval)
			field.customHandlers.click(field, control);
		return retval;
	}
	else
	{
		if (field.type.click != null)
			return field.type.click(field, control);
		else
			return true;
	}
}

function controlOnKeypress(control, event)
{
	field = activeDialog.fieldsById[control.name];
	if(typeof field == "undefined" || field == null || field.type == null) return;

	if (field.customHandlers.keyPress != null)
	{
		var retval = true;
		if (field.customHandlers.keyPressType == 'extends')
		{
			if (field.type.keyPress != null)
				retval =  field.type.keyPress(field, control);
		}

		if (retval)
			retval =  field.customHandlers.keyPress(field, control);
		return retval;
	}
	else
	{
		if (field.type.keyPress != null)
			return field.type.keyPress(field, control, event);
		else
			return true;
	}
}

function controlOnFocus(control, event)
{
	field = activeDialog.fieldsById[control.name];
	if(typeof field == "undefined" || field == null || field.type == null) return;

	if (activeDialog.hideHintsUntilFocus != null)
	{
	    if (activeDialog.hideHintsUntilFocus()== true)
	    {
            var hintFieldName = field.name + "_hint";

            var hintField  = document.getElementById(hintFieldName);
            if (hintField != null)
            {
                if (hintField.style)
                    hintField.style.display = 'block';
                else
                    hintField.visibility = 'show';
            }
        }
	}

	if (field.customHandlers.getFocus != null)
	{
		var retval = true;
		if (field.customHandlers.getFocusType == 'extends')
		{
			if (field.type.getFocus != null)
				retval =  field.type.getFocus(field, control);
		}
		if (retval)
			retval =  field.customHandlers.getFocus(field, control);
		return retval;
	}
	else
	{
		if (field.type.getFocus != null)
			return field.type.getFocus(field, control);
		else
			return true;
	}
}

function controlOnChange(control, event)
{
	anyControlChangedEventCalled = true;
	field = activeDialog.fieldsById[control.name];
	if(typeof field == "undefined" || field == null) return;

	if (field.scannable == 'yes')
	{
		field.isScanned = false;
		var validScan = scanField_changeDisplayValue(field, control);
		if(! validScan)
		{
			event.cancelBubble = true;
			event.returnValue = false;
			return false;
		}
	}

	if(field.dependentConditions.length > 0)
	{
		var conditionalFields = field.dependentConditions;
		for(var i = 0; i < conditionalFields.length; i++)
			conditionalFields[i].evaluate(activeDialog, control);
	}

	if(field.type == null) return;
	if (field.customHandlers.valueChanged != null)
	{
		var retval = true;
		if (field.customHandlers.valueChangedType == 'extends')
		{
			if (field.type.valueChanged != null)
				retval = field.type.valueChanged(field, control);
		}
		if (retval)
			retval =  field.customHandlers.valueChanged(field, control);
		return retval;
	}
	else
	{
		if (field.type.valueChanged != null)
			return field.type.valueChanged(field, control);
		else
			return true;
	}
}

function controlOnBlur(control, event)
{
	field = activeDialog.fieldsById[control.name];
	if(typeof field == "undefined" || field == null || field.type == null) return;

	if (activeDialog.hideHintsUntilFocus != null)
	{
	    if (activeDialog.hideHintsUntilFocus()== true)
	    {
            var hintFieldName = field.name + "_hint";

            var hintField  = document.getElementById(hintFieldName);
            if (hintField != null)
            {
                if (hintField.style)
                    hintField.style.display = 'none';
                else
                    hintField.visibility = 'hide';
            }
	    }
	}

	var retval = true;
	if (field.customHandlers.loseFocus != null)
	{
		if (field.customHandlers.loseFocusType == 'extends')
		{
			if (field.type.loseFocus != null)
			retval = field.type.loseFocus(field, control);
		}
		if (retval)
			retval =  field.customHandlers.loseFocus(field, control);
	}
	else
	{
		if (field.type.loseFocus != null)
			retval = field.type.loseFocus(field, control);
	}

	if(control.value != "" && field != null && field.submitOnBlur)
	{
		submitOnblur(field, control);
	}
	return retval;
}

function submitOnblur(field, control)
{
	if (control.value != '')
	{
        setAllowValidation(false);
        SHOW_DATA_CHANGED_MESSAGE_ON_LEAVE = false;

        var vFieldName = "_d."+ dialog.name + "." + FIELDNAME_VALIDATE_TRIGGER_FIELD;
        document.forms[control.form.name].elements[vFieldName].value = field.name;
        control.form.submit();
    }
}

//****************************************************************************
// Keyboard-management utility functions
//****************************************************************************

var KEYCODE_ENTER          = 13;
var KEYCODE_TAB            = 9;
var KEYCODE_BS             = 8;

var NUM_KEYS_RANGE         = [48,  57];
var PERIOD_KEY_RANGE       = [46,  46];
var SLASH_KEY_RANGE        = [47,  47];
var DASH_KEY_RANGE         = [45,  45];
var UPPER_ALPHA_KEYS_RANGE = [65,  90];
var LOW_ALPHA_KEYS_RANGE   = [97, 122];
var UNDERSCORE_KEY_RANGE   = [95,  95];
var COLON_KEY_RANGE        = [58, 58];
var ENTER_KEY_RANGE        = [13, 13];

//****************************************************************************
// Field-specific validation and keypress filtering functions
//****************************************************************************

function CurrencyField_onKeyPress(field, control, event)
{
	return keypressAcceptRanges(field, control, [NUM_KEYS_RANGE, DASH_KEY_RANGE, PERIOD_KEY_RANGE], event);
}

function CurrencyField_isValid(field, control)
{
	if(field.isRequired() && control.value.length == 0)
	{
		field.alertRequired(control);
		return false;
	}
	if (control.value.length > 0)
	{
		var test = testCurrency(field, control);
		if (test == false)
		{
			field.alertMessage(control, field.text_format_err_msg);
			return false;
		}
	}
	return true;
}

function CurrencyField_valueChanged(field, control)
{
	return formatCurrency(field, control);
}

function BooleanField_onClick(field, control)
{
	if (control.type == 'checkbox' || control.type == 'radio')
	{
		if(field.dependentConditions.length > 0)
		{
			var conditionalFields = field.dependentConditions;
			for(var i = 0; i < conditionalFields.length; i++)
			conditionalFields[i].evaluate(activeDialog, control);
		}
	}
	return true;
}

function TextField_onFocus(field, control)
{
	if (field.readonly == 'yes')
		control.blur();

	return true;
}

function TextField_valueChanged(field, control)
{
	if (field.uppercase == 'yes')
	{
		control.value = control.value.toUpperCase();
	}

	if(control.value == "")
		return true;

	return TextField_isValid(field, control);
}

function TextField_onKeyPress(field, control, event)
{
	if (field.identifier == 'yes')
	{
		return keypressAcceptRanges(field, control, [NUM_KEYS_RANGE, UPPER_ALPHA_KEYS_RANGE, UNDERSCORE_KEY_RANGE], event);
	}
	return true;
}

function TextField_isValid(field, control)
{
	if(field.isRequired() && control.value.length == 0)
	{
		field.alertRequired(control);
		return false;
	}

    /* 12.14.04 Bug# 490/768 JH - When there is no data yet in the system a value
        in the array of objects can be null, so the code below can complain generating
        a run-time error.
    */
    if (control.value != null && control.value.length > 0)
	{
		if (field.validValues)
		{
			var valid = false;
			for (k in field.validValues)
			{
				if (field.validValues[k] == control.value)
					valid = true;
			}
			if (valid == false)
			{
				field.alertMessage(control, "value '" + control.value + "' is not valid. ");
				if (CLEAR_TEXT_ON_VALIDATE_ERROR)
				{
				    control.value = "";
				}
				return false;
			}
		}

		if (field.text_format_pattern != null && (typeof field.text_format_pattern != "undefined")
			&& field.text_format_pattern != "")
		{
			var test = testText(field, control);
			if (test == false)
			{
				field.alertMessage(control, field.text_format_err_msg);
				if (CLEAR_TEXT_ON_VALIDATE_ERROR)
				{
				    control.value = "";
				}
				return false;
			}
		}
	}

	return true;
}

function PhoneField_valueChanged(field, control)
{
	return formatPhone(field, control);
}

function PhoneField_isValid(field, control)
{
	if(field.isRequired() && control.value.length == 0)
	{
		field.alertRequired(control);
		return false;
	}
	if (control.value.length > 0)
	{
		var test = testPhone(field, control);
		if (test == false)
		{
			field.alertMessage(control, field.text_format_err_msg);
			return false;
		}
	}
	return true;
}

function SocialSecurityField_valueChanged(field, control)
{
	return formatSSN(field, control);
}

function SocialSecurityField_isValid(field, control)
{
	if(field.isRequired() && control.value.length == 0)
	{
		field.alertRequired(control);
		return false;
	}
	if (control.value.length > 0)
	{
		var test = testSSN(field, control);
		if (test == false)
		{
			field.alertMessage(control, "Social Security Number must be in the correct format: 999-99-9999");
			return false;
		}
	}
	return true;
}

function IntegerField_onKeyPress(field, control, event)
{
	return keypressAcceptRanges(field, control, [NUM_KEYS_RANGE, DASH_KEY_RANGE], event);
}

function IntegerField_isValid(field, control)
{
	if(field.isRequired() && control.value.length == 0)
	{
		field.alertRequired(control);
		return false;
	}

	var intValue = control.value - 0;
	if(isNaN(intValue))
	{
		field.alertMessage(control, "'"+ control.value +"' is an invalid integer.");
		return false;
	}

	if(field.minValue != "" && control.value < field.minValue)
	{
		field.alertMessage(control, "Minimum value is " + field.minValue);
		return false;
	}
	if(field.maxValue != "" && control.value > field.maxValue)
	{
		field.alertMessage(control, "Maximum value is " + field.maxValue);
		return false;
	}

	return true;
}

function FloatField_onKeyPress(field, control, event)
{
	return keypressAcceptRanges(field, control, [NUM_KEYS_RANGE, DASH_KEY_RANGE, PERIOD_KEY_RANGE], event);
}

function FloatField_isValid(field, control)
{
	if(field.isRequired() && control.value.length == 0)
	{
		field.alertRequired(control);
		return false;
	}

	var floatValue = control.value - 0;
	if(isNaN(floatValue))
	{
		field.alertMessage(control, "'"+ control.value +"' is an invalid decimal.");
		return false;
	}
	return true;
}

function MemoField_isValid(field, control)
{
	if(field.isRequired() && control.value.length == 0)
	{
		field.alertRequired(control);
		return false;
	}

	maxlimit = field.maxLength;
	if (control.value.length > maxlimit)
	{
		field.alertMessage(control, "Maximum number of characters allowed is " + maxlimit);
		return false;
	}
	return true;
}

function MemoField_onKeyPress(field, control, event)
{
	maxlimit = field.maxLength;
	if (control.value.length >= maxlimit)
	{
		field.alertMessage(control, "Maximum number of characters allowed is " + maxlimit);
		return false;
	}
	return true;
}

function DateField_popupCalendar()
{
	showCalendar(this.getControl(activeDialog), 0);
}

function DateField_finalizeDefn(dialog, field)
{
	field.popupCalendar = DateField_popupCalendar;
	field.dateFmtIsKnownFormat = false;
	field.dateItemDelim = null;
	field.dateItemDelimKeyRange = null;
	if (field.dateDataType == DATE_DTTYPE_DATEONLY)
	{
		if (field.dateFormat == "MM/dd/yyyy" || field.dateFormat == "MM/dd/yy")
		{
			field.dateItemDelim = '/';
			field.dateItemDelimKeyRange = SLASH_KEY_RANGE;
			field.dateFmtIsKnownFormat = true;
		}
		else if (field.dateFormat == "MM-dd-yyyy" || field.dateFormat == "MM-dd-yy")
		{
			field.dateItemDelim = '-';
			field.dateItemDelimKeyRange = DASH_KEY_RANGE;
			field.dateFmtIsKnownFormat = true;
		}
	}
}

function DateField_isValid(field, control)
{
	if(field.isRequired() && control.value.length == 0)
	{
		field.alertRequired(control);
		return false;
	}

	return DateField_valueChanged(field, control);
}

function DateField_valueChanged(field, control)
{
	if (field.dateDataType == DATE_DTTYPE_DATEONLY && field.dateFmtIsKnownFormat)
	{
		var result = formatDate(field, control, field.dateItemDelim, field.dateStrictYear);
		control.value = result[1];
		return result[0];
	}
	else if (field.dateDataType == DATE_DTTYPE_TIMEONLY)
	{
		var result = formatTime(field, control);
		return result;
	}
	else if (field.dateDataType == DATE_DTTYPE_MONTH_YEAR_ONLY)
	{
	    var result = formatMonthYearDate(field, control, field.dateItemDelim, field.dateStrictYear);
		control.value = result[1];
		return result[0];
	}
	return true;
}

function DateField_onKeyPress(field, control, event)
{
	if (field.dateDataType == DATE_DTTYPE_DATEONLY && field.dateFmtIsKnownFormat)
	{
		return keypressAcceptRanges(field, control, [NUM_KEYS_RANGE, field.dateItemDelimKeyRange], event);
	}
	else if (field.dateDataType == DATE_DTTYPE_TIMEONLY)
	{
		return keypressAcceptRanges(field, control, [NUM_KEYS_RANGE, COLON_KEY_RANGE], event);
	}
	return true;
}

function SocialSecurityField_onKeyPress(field, control, event)
{
	return keypressAcceptRanges(field, control, [NUM_KEYS_RANGE, DASH_KEY_RANGE], event);
}

function SelectField_onClick(field, control)
{
    var fieldControl;
    // The onclick event handling is only here for RADIO buttons because the onChange doesn't work for them.
    if (field.style == SELECTSTYLE_RADIO)
    {
        // the 'control' object sent from the browser event actually represents just the one radio button and not
        // the array so get the array control object by using the common name shared by all the radio buttons.
        fieldControl = field.getControl(dialog);
        if(field.dependentConditions.length > 0)
        {
            var conditionalFields = field.dependentConditions;
            for(var i = 0; i < conditionalFields.length; i++)
                conditionalFields[i].evaluate(activeDialog, fieldControl);
        }
    }
    return true;
}

function SelectField_loseFocus(field, control)
{
	if(control.value == "")
		return true;

	//return SelectField_isValid(field, control);
	return this.isValid(field, control);
}

function SelectField_isValid(field, control)
{
	var style = field.style;
	if (style == SELECTSTYLE_POPUP)
	{
		if (field.isRequired() && control.value.length == 0)
		{
			field.alertRequired(control);
			return false;
		}

		if (control.value.length > 0 && field.choicesCaption)
		{
			var valid = -1;

			for (var i=0; i < field.choicesCaption.length; i++)
			{
				if (field.choicesCaption[i] == control.value)
					valid = i;
			}
			if (valid < 0)
			{
				field.alertMessage(control, "Entered field value '" + control.value + "' is not valid. ");
				if (CLEAR_TEXT_ON_VALIDATE_ERROR)
				{
				    control.value = "";
				}
				return false;
			}
			else
			{
				adjacentArea = field.getAdjacentArea(activeDialog);
				if(adjacentArea != null)
				{
					// alert("Adjacent set to " + field.choicesValue[valid]);
					adjacentArea.innerHTML = field.choicesValue[valid];
				}
				return true;
			}
		}
	}

	if(field.isRequired())
	{
		if(style == SELECTSTYLE_RADIO)
		{
			if(control.value == '')
			{
				field.alertRequired(control[0]);
				return false;
			}
		}
		else if(style == SELECTSTYLE_COMBO)
		{
			if(field.isRequired() && control.options[control.selectedIndex].value.length == 0)
		{
			field.alertRequired(control);
			return false;
		}
		}
		else if(style == SELECTSTYLE_LIST || style == SELECTSTYLE_MULTILIST)
		{
			var selectedCount = getSelectedCount(control);
			if(selectedCount == 0)
			{
				field.alertRequired(control);
				return false;
			}
		}
		else if(style == SELECTSTYLE_MULTICHECK)
		{
			var selectedCount = getCheckedCount(control);
			if(selectedCount == 0)
			{
				field.alertRequired(control[0]);
				return false;
			}
		}
		else if(style == SELECTSTYLE_MULTIDUAL)
		{
			var selectedCount = getSelectedCount(control);
			if(selectedCount == 0)
			{
				field.alertRequired(control);
				return false;
			}
		}
	}
	return true;
}

addFieldType("com.netspective.sparx.form.field.type.TextField", null, TextField_isValid, null, TextField_onFocus, TextField_valueChanged, null, null);
addFieldType("com.netspective.sparx.form.field.type.SelectField", null, SelectField_isValid, null, null, SelectField_loseFocus, null, SelectField_onClick);
addFieldType("com.netspective.sparx.form.field.type.BooleanField", null, null, null, null, null, null, BooleanField_onClick);
addFieldType("com.netspective.sparx.form.field.type.MemoField", null, MemoField_isValid, null, null, null, MemoField_onKeyPress);
addFieldType("com.netspective.sparx.form.field.type.DateTimeField", DateField_finalizeDefn, DateField_isValid, null, null, DateField_valueChanged, DateField_onKeyPress, null);
addFieldType("com.netspective.sparx.form.field.type.IntegerField", null, IntegerField_isValid, null, null, null, IntegerField_onKeyPress, null);
addFieldType("com.netspective.sparx.form.field.type.FloatField", null, FloatField_isValid, null, null, null, FloatField_onKeyPress, null);
addFieldType("com.netspective.sparx.form.field.type.SocialSecurityField", null, SocialSecurityField_isValid, null, null, SocialSecurityField_valueChanged, SocialSecurityField_onKeyPress, null);
addFieldType("com.netspective.sparx.form.field.type.PhoneField", null, PhoneField_isValid, null, null, PhoneField_valueChanged, null, null);
addFieldType("com.netspective.sparx.form.field.type.CurrencyField", null, CurrencyField_isValid, CurrencyField_valueChanged, null, null, null, null);

//****************************************************************************
// Date Formatting
//****************************************************************************

var VALID_NUMBERS =  ["0","1","2","3","4","5","6","7","8","9"];

// This method is to demonstrate calculating the total of the first four columns in a grid row and
// setting the fifth column to the total value
function testGridRow(field, control)
{
    gridRowField = activeDialog.fieldsByQualName[field.parentName];
    var total = 0;

    // last field is the total field
    for (var i=0; i < gridRowField.childrenNames.length-1; i++)
    {
        total = total + parseInt(activeDialog.getFieldControl(gridRowField.childrenNames[i]).value);
    }
    activeDialog.getFieldControl(gridRowField.childrenNames[gridRowField.childrenNames.length-1]).value = total;
}

function testText(field, control)
{
	var pattern = field.text_format_pattern;
	if (control.value == '' || pattern == '')
		return true;
   	return pattern.test(control.value);
}

function testCurrency(field, control)
{
	if (control.value == '')
		return true;
	var pattern = field.text_format_pattern;
	return pattern.test(control.value) ;
}

function formatCurrency(field, control)
{
	var test = testCurrency(field, control);
	if (test == false)
	{
		field.alertMessage(control, this.field.text_format_err_msg);
		return false;
	}
	else
	{
		if (control.value != '')
		{
			value = control.value;
			var pattern = field.text_format_pattern;
			if (pattern.exec(value))
			{
				match = pattern.exec(value)
				if (field.negative_pos == "after")
				{
					if (match[1] == "")
						match[1] = field.currency_symbol;
					if (typeof match[3] == "undefined")
						match[3] = ".00";
					control.value = match[1] + match[2] + match[3];
				}
				else if (field.negative_pos == "before")
				{
					if (match[2] == "")
						match[2] = field.currency_symbol;
					if (typeof match[4] == "undefined")
						match[4] = ".00";
					control.value = match[1] + match[2] + match[3] + match[4];
				}
			}
		}
	}
	return true;
}

function testPhone(field, control)
{
	if (control.value == '')
		return true;
	var phonePattern = field.text_format_pattern;
	return phonePattern.test(control.value) ;
}

function formatPhone(field, control)
{
	var test = testPhone(field, control);
	if (test == false)
	{
		field.alertMessage(control, field.text_format_err_msg);
		return false;
	}
	else
	{
		if (control.value != '')
		{
			var phoneStr = control.value;
			if (field.phone_format_type == 'dash')
			{
				phoneStr = phoneStr.replace(field.text_format_pattern, "$1-$2-$3$4");
			}
			else
			{
				phoneStr = phoneStr.replace(field.text_format_pattern, "($1) $2-$3$4");
			}
			control.value = phoneStr;
		}
	}
	return true;
}

function testSSN(field, control)
{
	if (control.value == '')
		return true;
	var ssnPattern = field.text_format_pattern ;
	return ssnPattern.test(control.value);
}

function formatSSN(field, control)
{
	var test = testSSN(field, control);
	if (test == false)
	{
		field.alertMessage(control, "Social Security Number must be in the correct format: 999-99-9999");
		return false;
	}
	if (control.value != '')
	{
		var ssn = control.value;
		ssn = ssn.replace(field.text_format_pattern, "$1-$2-$3");
		control.value = ssn;
	}
	return true;
}


function testTime(field, control)
{
	var inTime = control.value;
	if (inTime == '')
		return true;
	var hr = null;
	var min = null;
	if (inTime.length == 5 && inTime.indexOf(":") == 2)
	{
		hr = inTime.substring(0, 2);
		min = inTime.substring(3);
		if (hr > 23 || min > 59)
		{
			field.alertMessage(control, "Time field must have a valid value");
			return false;
		}
		return true;
	}
	else if (inTime.length == 4 && inTime.indexOf(":") == 1)
	{
		hr = inTime.substring(0, 1);
		min = inTime.substring(2);
		if (hr > 23 || min > 59)
		{
			field.alertMessage(control, "Time field must have a valid value");
			return false;
		}
		return true;
	}
	field.alertMessage(control, "Time field must have the correct format: " + field.dateFormat);
	return false;
}

function formatTime(field, control)
{
	var inTime = control.value;
	newTime = inTime;
	if (field.timeStrict == false && inTime.indexOf(":") == -1)
	{
		if (inTime.length == 4)
		{
			newTime = inTime.substring(0, 2) + ":"  + inTime.substring(2);
		}
		else if (inTime.length == 3)
		{
			newTime = inTime.substring(0, 1) + ":" + inTime.substring(1);
		}
		control.value = newTime;
	}
	return testTime(field, control);
}


function formatMonthYearDate(field, control, delim, strictYear)
{
    var formattedDate;
    if (delim == null)
        delim = "/";

    var today = new Date();
    var currentYear = today.getFullYear().toString();
    var fmtMessage = "Date must be in correct format: MM" + delim + "YYYY'";

    // matches 2 or 4 digit years
    var yearMatchExpr = "(\\d{4}|\\d{2})";
    if (strictYear)
        yearMatchExpr = "(\\d{4})";

    var regEx = new RegExp("(\\d{1,2})(" + delim + ")?" + yearMatchExpr);
    var m = regEx.exec(control.value);
    if (m != null)
    {
        // remember that the first index is the whole value!
        if (m.length == 3)
        {
            // make sure the month is valid
            var month = parseInt(m[1]);
            var year = parseInt(m[2]);

            if ( (month < 1) || (month > 12) )
            {
                field.alertMessage(control, "Month value must be between 1 and 12");
                return [false,control.value];
            }
            // if the year entered was a 2-digit year convert it to a four digit one
            if (m[2].length == 2)
                m[2] = currentYear.substring(0,2) + m[2];
            formattedDate = m[1] + delim + m[2];
        }
        else
        {
            var month = parseInt(m[1]);
            var year = parseInt(m[2]);
            if ( (month < 1) || (month > 12) )
            {
                field.alertMessage(control, "Month value must be between 1 and 12");
                return [false,control.value];
            }
            if (m[3].length == 2)
                m[3] = currentYear.substring(0,2) + m[3];
            formattedDate = m[1] + delim + m[3];
        }
        //control.value = formattedDate;

    }
    else
    {
        field.alertMessage(control, fmtMessage);
        return [false,control.value];
    }
    return [true,formattedDate];
}

function formatDate(field, control, delim, strictYear)
{
	if (delim == null)
		delim = "/";

	var inDate = control.value;
	var today = new Date();
	var currentDate = today.getDate();
	var currentMonth = today.getMonth() + 1;
	var currentYear = today.getYear();
	var fmtMessage = "Date must be in correct format: 'D', 'M" + delim + "D', 'M" + delim + "D" + delim + "Y', or 'M" + delim + "D" + delim + "YYYY'";

	inDate = inDate.toLowerCase();
	var a = splitNotInArray(inDate, VALID_NUMBERS);
	for (i in a)
	{
		a[i] = '' + a[i];
	}
	if (a.length == 0)
	{
		if (inDate.length > 0)
			field.alertMessage(control, fmtMessage);
		return [true, inDate];
	}
	if (a.length == 1)
	{
		if ((a[0].length == 6) || (a[0].length == 8))
		{
			a[2] = a[0].substring(4);
			a[1] = a[0].substring(2,4);
			a[0] = a[0].substring(0,2);
		}
		else
		{
			if (a[0] == 0)
			{
				a[0] = currentMonth;
				a[1] = currentDate;
			}
			else
			{
				a[1] = a[0];
				a[0] = currentMonth;
			}
		}
	}
	if (a.length == 2)
	{
		if (a[0] <= (currentMonth - 3))
			a[2] = currentYear + 1;
		else
			a[2] = currentYear;
	}

	if (strictYear != true)
	{
		if (a[2] < 100 && a[2] > 10)
			a[2] = "19" + a[2];
		if (a[2] < 1000)
			a[2] = "20" + a[2];
	}
	if ( (a[0] < 1) || (a[0] > 12) )
	{
		field.alertMessage(control, "Month value must be between 1 and 12");
		return [false, inDate];
	}
	if ( (a[1] < 1) || (a[1] > 31) )
	{
		field.alertMessage(control, "Day value must be between 1 and 31");
		return [false, inDate];
	}
	if ( (a[2] < 1800) || (a[2] > 2999) )
	{
		field.alertMessage(control, "Year must be between 1800 and 2999");
		return [false, inDate];
	}
	return [true, padZeros(a[0],2) + delim + padZeros(a[1],2) + delim + a[2]];
}

// --------------------------------------------
function getDoubleEntries(field, control)
{
	if (field.successfulEntry) return true;
	if (field.scannable == 'yes' && field.isScanned)
	{
		field.successfulEntry = true;
		return true;
	}

	if(field.firstEntryValue == "")
	{
		field.firstEntryValue = control.value;
		if(field.firstEntryValue == "") field.successfulEntry = true;
		control.value = "";
		control.focus();
		return false;
	}
	else
	{
		if (field.firstEntryValue != control.value)
		{
			control.value = "";
			field.alertMessage(control, "Double Entries do not match.  Previous entry = '"
				+ field.firstEntryValue + "'");
			field.firstEntryValue = "";
		}
		else
		{
			field.successfulEntry = true;
			field.firstEntryValue = "";
			return true;
		}
	}
}

function doubleEntry(field, control)
{
	var result = getDoubleEntries(field, control);
	window.event.cancelBubble = true;
	window.event.returnValue = false;
	return result;
}

// --------------------------------------------
function scanField_changeDisplayValue(field, control)
{
	var beginPattern = new RegExp("^" + field.scanStartCode, field.scanCodeIgnoreCase);
	var endPattern   = new RegExp(field.scanStopCode + "$", field.scanCodeIgnoreCase);

	var newValue = control.value.replace(beginPattern, "");
	newValue = newValue.replace(endPattern, "");

	field.isScanned = (beginPattern.test(control.value) && endPattern.test(control.value)) ? true : false;

	if(field.scanPartnerField != "")
	{
		var partnerField = dialog.fieldsByQualName[field.scanPartnerField];
		var partnerControl = partnerField.getControl(dialog);
		partnerControl.value = (field.isScanned) ? 'yes' : 'no';
	}

	if(field.isScanned && field.scanFieldCustomScript != "")
	{
		newValue = field.scanFieldCustomScript(field, control, newValue);
	}

	control.value = newValue;
	return (newValue != "") ? true : false;
}

// --------------------------------------------------------------------------
// Methods for handling actions with respect to reports
// --------------------------------------------------------------------------

// These report action types MUST BE in sync with their JAVA conuterparts
var EXECUTE_SELECT_REPORT_ACTION   = 0;
var EXECUTE_RS_NEXT_REPORT_ACTION  = 1;
var EXECUTE_RS_PREV_REPORT_ACTION  = 2;
var EXECUTE_RS_FIRST_REPORT_ACTION = 3;
var EXECUTE_RS_DONE_REPORT_ACTION  = 4;
var EXECUTE_RS_LAST_REPORT_ACTION  = 5;

/**
 * This function submits the  form associated with the report
 */
function ReportAction_submit(actionType, redirectUrl)
{
    if (activeDialog != null)
    {
        var dialog = eval("document.forms." + activeDialog.name);
        if (actionType == EXECUTE_SELECT_REPORT_ACTION && redirectUrl != null)
            dialog.action = redirectUrl;
        alert(redirectUrl);
        dialog.submit();
        return true;
    }
}

/**
 * This function changes the color of the parent row of the passed in source item
 */
function ReportAction_highlightRow(source, color)
{
	while (source.tagName.toUpperCase() != 'TR' && source != null)
		source = document.all ? source.parentElement : source.parentNode;
	if (source)
	{
		source.bgColor = color;
		source.style.background = color;
	}
}

/**
 * This function allows you to select a row by clicking on a checkbox. THe value assigned to the
 * checkbox is saved to a 'selected items' list.
 */
function ReportAction_selectRow(source, fieldName)
{
	// fieldName  is the name of the dialog field where the list of selected items are stored
	// and value is the value to add to or remove from the list
	var control = activeDialog.getFieldControl(fieldName);
	if(control == null || typeof control == "undefined")
	{
		alert("Field '" + fieldName + "' not found in active dialog -- can't check for selected values");
		return false;
	}
	value = source.value;
	if (source.checked)
	{
		ReportAction_highlightRow(source, "#ccd9e5");
	    var intLength = 0;
	    if (control.options)
	        intLength = control.options.length;

		var newOption = true;
		for (var i=0; i < intLength; i++)
		{
			if(control.options[i] != null && control.options[i].value == value)
			{
				newOption = false;
			}
		}

		if (newOption == true)
		{
			// create a new entry to the selected item list
			var objNewOpt = new Option();
			objNewOpt.value = value;
			objNewOpt.text = value;
			objNewOpt.selected = true;
			control.options[control.options.length] = objNewOpt;
		}
	}
	else
	{
		ReportAction_highlightRow(source, "#ffffff");
	    var intLength = control.options.length;
		// remove entry from the selected item list
		for (var i=0; i < intLength; i++)
		{
			if(control.options[i] != null && control.options[i].value == value)
			{
				control.options[i] = null;
			}
		}
	}
	return true;
}

//****************************************************************************
// Event handlers
//****************************************************************************

function _documentOnKeyDown()
{
	var control = window.event.srcElement;
	var field = activeDialog.fieldsById[control.name];
	if (field != null)
	{
		if(window.event.keyCode == KEYCODE_ENTER && field.doubleEntry == 'yes')
		{
			control.blur();
			if(field.focusNext(activeDialog))
			{
				window.event.cancelBubble = true;
				window.event.returnValue = false;
				return false;
			}
		}
	}

	if(TRANSLATE_ENTER_KEY_TO_TAB_KEY && window.event.keyCode == KEYCODE_ENTER)
	{
		if(field == null)
		{
			alert("Control '"+ control.srcElement.name + "' was not found in activeDialog.fieldsById");
			window.event.returnValue = false;
			return false;
		}

		if(field.focusNext(activeDialog))
		{
			window.event.cancelBubble = true;
			window.event.returnValue = false;
			return false;
		}
	}

	return true;
}

// --------------------------------------------
function documentOnLeave()
{
    if(activeDialog == null)
        return;

	if(SHOW_DATA_CHANGED_MESSAGE_ON_LEAVE && anyControlChangedEventCalled && ! submittedDialogValid)
		return "You have changed data on this page. If you leave, you will lose the data.";
}

// --------------------------------------------
function documentOnKeyDown()
{
    if(activeDialog == null)
        return;

	var control = window.event.srcElement;
	var field = activeDialog.fieldsById[control.name];

	if(window.event.keyCode == KEYCODE_ENTER)
	{
		if(TRANSLATE_ENTER_KEY_TO_TAB_KEY)
		{
			if(control.type == "submit")
			{
				control.click();
				return true;
			}
			window.event.keyCode = KEYCODE_TAB;
		}
		else if(field != null && field.doubleEntry == "yes")
		{
			window.event.keyCode = KEYCODE_TAB;
		}
	}

	return true;
}

// --------------------------------------------
function documentOnKeyUp()
{
    if(activeDialog == null)
        return;

	var control = window.event.srcElement;
	var field = activeDialog.fieldsById[control.name];

	if(field != null && field.autoBlur == "yes")
	{
		field.numCharsEntered++;
		var excRegExp = new RegExp(field.autoBlurExcRegExp, "g");
		var adjustedVal = control.value.replace(excRegExp, "");

		var beginPattern = new RegExp("^" + field.scanStartCode, field.scanCodeIgnoreCase);
		if(control.value.search(beginPattern) != -1)
		{
			var startCodeLength = field.scanStartCode.indexOf("|") != -1 ?
				field.scanStartCode.indexOf("|") : field.scanStartCode.length;
			var stopCodeLength  = field.scanStopCode.indexOf("|") != -1 ?
				field.scanStopCode.indexOf("|") : field.scanStopCode.length;

			if((adjustedVal.length == field.autoBlurLength + startCodeLength + stopCodeLength)
				 && field.numCharsEntered >= field.autoBlurLength -1)
			{
				field.numCharsEntered = 0;

				// --------------------------------------
				if (field.scannable == 'yes')
				{
					field.isScanned = false;
					var validScan = scanField_changeDisplayValue(field, control);
					if(! validScan)
					{
						control.value = "";
						return false;
					}
				}
				// --------------------------------------

				field.focusNext(activeDialog);
			}
		}
		else
		{
			if(adjustedVal.length == field.autoBlurLength && field.numCharsEntered >= field.autoBlurLength -1)
			{
				field.numCharsEntered = 0;
				field.focusNext(activeDialog);
			}
		}
	}
}

document.onkeydown = documentOnKeyDown;
document.onkeyup   = documentOnKeyUp;
window.onbeforeunload = documentOnLeave;

dialogLibraryLoaded = true;


/**
 * THE FOLLOWING SECTION CONTAINS JAVASCRIPT FUNCTIONS THAT ARE MORE GENERIC IN NATURE
 * ----------------------------------------------------------------------------------------------------
 */

/**
 * ------------------------------------------------------------------------------------------------------
 * DESCRIPTION:
 *     This function checks a group of checkboxes with the same name to see if a
 *     checked checkbox exists with a particular value.
 * ------------------------------------------------------------------------------------------------------
 * INPUT:
 *     checkbox:    checkbox(s) form element
 *     value:       value to look for
 * ------------------------------------------------------------------------------------------------------
 * RETURNS:
 *     True if one or more checked checkbox has the value else False
 * ------------------------------------------------------------------------------------------------------
 */
function checkedCheckedValue(checkbox, value)
{
    if (checkbox.length)
    {
        // multiple checkboxes with the same name
        for(var i = 0; i < checkbox.length; i++)
        {
            if (checkbox[i].checked && checkbox[i].value == value)
                return true;
        }
    }
    else
    {
        // only one checkbox
        if (checkbox.checked && checkbox.value == value)
            return true;
    }
    return false;
}

/**
 * ------------------------------------------------------------------------------------------------------
 * DESCRIPTION:
 *     This function gets the total number of checkboxes that are checked
 * ------------------------------------------------------------------------------------------------------
 * INPUT:
 *     checkBox:    checkbox(s) form element
 * ------------------------------------------------------------------------------------------------------
 * RETURNS:
 *     Zero if no checkboxes are checked
 * ------------------------------------------------------------------------------------------------------
 */
function getCheckedCount(checkbox)
{
    var selectedCount = 0;
    if (checkbox.length)
    {
        // multiple checkboxes with same name
        for(var c = 0; c < checkbox.length; c++)
        {
            if(checkbox[c].checked)
                selectedCount++;
        }
    }
    else
    {
        // one checkbox
        if (checkbox.checked)
            selectedCount = 1;
    }
    return selectedCount;
}

/**
 * ------------------------------------------------------------------------------------------------------
 * DESCRIPTION:
 *     This function gets the total number of options that are selected in an HTML SELECT element
 * ------------------------------------------------------------------------------------------------------
 * INPUT:
 *     select:    SELECT form element
 * ------------------------------------------------------------------------------------------------------
 * RETURNS:
 *     Zero if no options are selected
 * ------------------------------------------------------------------------------------------------------
 */
function getSelectedCount(select)
{
    var selectedCount = 0;
    var options = select.options;
    for(var o = 0; o < options.length; o++)
    {
        if(options[o].selected)
            selectedCount++;
    }
    return selectedCount;
}

/*
 * ------------------------------------------------------------------------------------------------------
 * DESCRIPTION:
 *     Sorts a select box. Uses a simple sort.
 * ------------------------------------------------------------------------------------------------------
 * INPUT:
 *	   objSelect = A <SELECT> object.
 * ------------------------------------------------------------------------------------------------------
 * RETURNS:
 *     None
 * ------------------------------------------------------------------------------------------------------
 * NOTE:
 *    Refactored from dialog.js and used for SelectField MultiDual support
 * ------------------------------------------------------------------------------------------------------
 */
function SimpleSort(objSelect)
{
	var arrTemp = new Array();
	var objTemp = new Object();
	var valueTemp = new Object();
	for(var i=0; i<objSelect.options.length; i++)
	{
		arrTemp[i] = objSelect.options[i];
	}

	for(var x=0; x<arrTemp.length-1; x++)
	{
		for(var y=(x+1); y<arrTemp.length; y++)
		{
			if(arrTemp[x].text > arrTemp[y].text)
			{
				objTemp = arrTemp[x].text;
				arrTemp[x].text = arrTemp[y].text;
				arrTemp[y].text = objTemp;

				valueTemp = arrTemp[x].value;
				arrTemp[x].value = arrTemp[y].value;
				arrTemp[y].value = valueTemp;
			}
		}
	}
	for(var i=0; i<objSelect.options.length; i++)
	{
		alert(objSelect.options[i].text + " " + objSelect.options[i].value);
	}
}

/*
 * ------------------------------------------------------------------------------------------------------
 * DESCRIPTION:
 *    Removes empty select items. This is a helper function for MoveSelectItems.
 * ------------------------------------------------------------------------------------------------------
 * INPUT:
 *     objSelect = A <SELECT> object.
 *     intStart = The start position (zero-based) search. Optimizes the recursion.
 * ------------------------------------------------------------------------------------------------------
 * RETURNS:
 *     None
 * ------------------------------------------------------------------------------------------------------
 * NOTE:
 *    Refactored from dialog.js and used for SelectField MultiDual support
 * ------------------------------------------------------------------------------------------------------
 */
function RemoveEmpties(objSelect, intStart)
{
	for(var i=intStart; i<objSelect.options.length; i++)
	{
		if (objSelect.options[i].value == "")
		{
			objSelect.options[i] = null;    // This removes item and reduces count
			RemoveEmpties(objSelect, i);
			break;
		}
	}
}

/*
 * ------------------------------------------------------------------------------------------------------
 * DESCRIPTION:
 *    Moves items from one select box to another.
 * ------------------------------------------------------------------------------------------------------
 * INPUT:
 *     strFormName = Name of the form containing the <SELECT> elements
 *     strFromSelect = Name of the left or "from" select list box.
 *     strToSelect = Name of the right or "to" select list box
 *     blnSort = Indicates whether list box should be sorted when an item(s) is added
 * ------------------------------------------------------------------------------------------------------
 * RETURNS:
 *     none
 * ------------------------------------------------------------------------------------------------------
*/
function MoveSelectItems(strFormName, strFromSelect, strToSelect, blnSort)
{
	var dialog = eval("document.forms." + strFormName);
	var objSelectFrom = dialog.elements[strFromSelect];
	var objSelectTo = dialog.elements[strToSelect];
	var intLength = objSelectFrom.options.length;

	for (var i=0; i < intLength; i++)
	{
		if(objSelectFrom.options[i].selected && objSelectFrom.options[i].value != "")
		{
			var objNewOpt = new Option();
			objNewOpt.value = objSelectFrom.options[i].value;
			objNewOpt.text = objSelectFrom.options[i].text;
			objSelectTo.options[objSelectTo.options.length] = objNewOpt;
			objSelectFrom.options[i].value = "";
			objSelectFrom.options[i].text = "";
		}
	}

	if (blnSort) SimpleSort(objSelectTo);
	RemoveEmpties(objSelectFrom, 0);
}

/*
 * ------------------------------------------------------------------------------------------------------
 * DESCRIPTION:
 *    Checks to see if the key pressed is allowed
 * ------------------------------------------------------------------------------------------------------
 * INPUT:
 *     field
 *     control
 *     acceptKeyRanges:     array of ascii values
 *     event:               the key press event
 * ------------------------------------------------------------------------------------------------------
 * RETURNS:
 *     True if the originating key is within the accepted key range else False
 * ------------------------------------------------------------------------------------------------------
 * NOTES:
 *     This function has IE-specific code
 * ------------------------------------------------------------------------------------------------------
 */
function keypressAcceptRanges(field, control, acceptKeyRanges, event)
{
	//if(! ENABLE_KEYPRESS_FILTERS)
	//	return true;

	// the event should have been passed in here but for some reason
	// its null, look for it in the window object (works only in IE)
	if (event == null || typeof event == "undefined")
		event = window.event;
	for (i = 0; i < acceptKeyRanges.length; i++)
	{
		var keyCodeValue = null;
		if (event.keyCode)
			keyCodeValue = event.keyCode;
		else
			keyCodeValue = event.which;

		var keyInfo = acceptKeyRanges[i];
		if(keyCodeValue >= keyInfo[0] && keyCodeValue <= keyInfo[1])
			return true;
	}

	// if we get to here, it means we didn't accept any of the ranges
	if (event.cancelBubble)
	    event.cancelBubble = true;
	event.returnValue = false;
	return false;
}

/**
 * ------------------------------------------------------------------------------------------------------
 * DESCRIPTION:
 *     This function returns a string of exactly count characters left padding with zeros
 * ------------------------------------------------------------------------------------------------------
 * INPUT:
 *     number:  string to pad
 *     count:   number of zero paddings
 * ------------------------------------------------------------------------------------------------------
 * RETURNS:
 *     returns a string of exactly count characters left padding with zeros
 * ------------------------------------------------------------------------------------------------------
 */
function padZeros(number, count)
{
	var padding = "0";
	for (var i=1; i < count; i++)
		padding += "0";
	if (typeof(number) == 'number')
		number = number.toString();
	if (number.length < count)
		number = (padding.substring(0, (count - number.length))) + number;
	if (number.length > count)
		number = number.substring((number.length - count));
	return number;
}

/**
 * ------------------------------------------------------------------------------------------------------
 * DESCRIPTION:
 *     This function splits "string" into multiple tokens at "char"
 * ------------------------------------------------------------------------------------------------------
 * INPUT:
 *     strString:       string to parse
 *     strDelimiter:    the token
 * ------------------------------------------------------------------------------------------------------
 * RETURNS:
 *     returns an array of substrings
 * ------------------------------------------------------------------------------------------------------
 */
function splitOnChar(strString, strDelimiter)
{
	var a = new Array();
	var field = 0;
	for (var i = 0; i < strString.length; i++)
	{
		if ( strString.charAt(i) != strDelimiter )
		{
			if (a[field] == null)
				a[field] = strString.charAt(i);
			else
				a[field] += strString.charAt(i);
		}
		else
		{
			if (a[field] != null)
				field++;
		}
	}
	return a;
}

/**
 * ------------------------------------------------------------------------------------------------------
 * DESCRIPTION:
 *     This function Splits "strString" into multiple tokens at inverse of "array"
 * ------------------------------------------------------------------------------------------------------
 * INPUT:
 *     strString:       string to parse
 *     arrArray:        an array of characters
 * ------------------------------------------------------------------------------------------------------
 * RETURNS:
 *     returns an array of substrings
 * ------------------------------------------------------------------------------------------------------
 */
function splitNotInArray(strString, arrArray)
{
	var a = new Array();
	var field = 0;
	var matched;
	for (var i = 0; i < strString.length; i++)
	{
		matched = 0;
		for (k in arrArray)
		{
			if (strString.charAt(i) == arrArray[k])
			{
				if (a[field] == null || typeof a[field] == "undefined")
					a[field] = strString.charAt(i);
				else
					a[field] += strString.charAt(i);
				matched = 1;
				break;
			}
		}
		if ( matched == 0 && a[field] != null )
			field++;
	}
	return a;
}



