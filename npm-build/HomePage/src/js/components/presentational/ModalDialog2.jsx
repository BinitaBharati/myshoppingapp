import React from "react";
import PropTypes from "prop-types";
import Button from "./Button.jsx";

/**
 Bootstrap vertically centered modal dialog. Ref : https://getbootstrap.com/docs/4.0/components/modal/
*/
 
const ModalDialog2 = ({ klass, id, ariaLabel, modalBody, errorMsgJsx, modalCloseBtnOnClickHandler, cssStyle}) => (
  <div className={klass} id={id} tabindex="-1" role="dialog" aria-labelledby={ariaLabel} aria-hidden="true" style={cssStyle}>
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id={ariaLabel}>Search Results</h5>
        <button type="button" class="close myModalClose" data-dismiss="modal" aria-label="Close" onClick={modalCloseBtnOnClickHandler}>
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
	  {modalBody}
      </div>
	  <div id="error-msg-div">{errorMsgJsx}</div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary myModalClose" data-dismiss="modal" onClick={modalCloseBtnOnClickHandler}>Close</button>
      </div>
    </div>
  </div>
</div>);
ModalDialog2.propTypes = {
  klass: PropTypes.string.isRequired,
  id: PropTypes.string.isRequired,
  ariaLabel: PropTypes.string.isRequired,
  modalBody: PropTypes.func.isRequired,
  errorMsgJsx: PropTypes.func,//optional
  modalCloseBtnOnClickHandler: PropTypes.func.isRequired,
  cssStyle: PropTypes.string 
};
export default ModalDialog2;
