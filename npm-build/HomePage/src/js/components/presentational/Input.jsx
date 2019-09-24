import React from "react";
import PropTypes from "prop-types";
const Input = ({ label, text, type, id, value, handleChange, name, placeHolder }) => (
  <div className="form-label-group">   
    <input
      type={type}
      className="form-control"
      id={id}
      value={value}
      onChange={handleChange}
	  name={name}
	  placeholder={placeHolder}
      required
    />
	<label htmlFor={label}>{text}</label>
  </div>
);
Input.propTypes = {
  label: PropTypes.string.isRequired,
  text: PropTypes.string.isRequired,
  type: PropTypes.string.isRequired,
  id: PropTypes.string.isRequired,
  value: PropTypes.string.isRequired,
  handleChange: PropTypes.func.isRequired,
  name: PropTypes.string.isRequired,
  placeHolder: PropTypes.string.isRequired
};
export default Input;
   