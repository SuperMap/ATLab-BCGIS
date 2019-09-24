import React from 'react';
import ReactDOM from 'react-dom';
import ToolBox from './Toolbox';
import Publish from './Publish';
import './index.css';

ReactDOM.render(<ToolBox />, document.getElementById('toolbox'));
ReactDOM.render(<Publish />, document.getElementById('map'));