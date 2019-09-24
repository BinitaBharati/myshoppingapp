import React from "react";
import PropTypes from "prop-types";
import Button from "./Button.jsx";

/**
 Bootstrap vertically centered modal dialog. Ref : https://getbootstrap.com/docs/4.0/components/modal/
*/
 
const ModalDialog = ({ klass, id, ariaLabel, modalBody, errorMsgJsx, actionButtonLabel, actionBtnOnClickHandler}) => (
  <div className={klass} id={id} tabindex="-1" role="dialog" aria-labelledby={ariaLabel} aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id={ariaLabel}>{actionButtonLabel}</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
	  {modalBody}
      </div>
	  <div id="error-msg-div">{errorMsgJsx}</div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
		<Button type="button" text={actionButtonLabel} klass="btn btn-primary" handleClick={actionBtnOnClickHandler} />
      </div>
    </div>
  </div>
</div>);
ModalDialog.propTypes = {
  klass: PropTypes.string.isRequired,
  id: PropTypes.string.isRequired,
  ariaLabel: PropTypes.string.isRequired,
  modalBody: PropTypes.func.isRequired,
  errorMsgJsx: PropTypes.func,//optional
  actionButtonLabel: PropTypes.string.isRequired,
  actionBtnOnClickHandler: PropTypes.func.isRequired
};
export default ModalDialog;
