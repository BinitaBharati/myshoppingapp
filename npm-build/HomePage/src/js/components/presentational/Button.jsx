import React from "react";
import PropTypes from "prop-types";
const Button2 = ({ klass, type, text, handleClick}) => (
  <button className={klass} type={type} onClick={handleClick}>{text}</button>);
Button2.propTypes = {
  klass: PropTypes.string.isRequired,
  type: PropTypes.string.isRequired,
  text: PropTypes.string.isRequired,
  handleClick: PropTypes.func.isRequired
};
export default Button2;
