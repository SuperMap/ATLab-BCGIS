import React from 'react';
import ReactDOM from 'react-dom';
import MapClass from './map';
import Publish from './publish';

class ToolBox extends React.Component {
    showPublish() {
        return (
            ReactDOM.render(<Publish />, document.getElementById('map'))
        );
    }

    showMap() {
        return (
            ReactDOM.render(<MapClass />, document.getElementById('map'))
        );
    }

    render() {
        return (
            <div className="buttons">
                <button id="publish" onClick={this.showPublish} >发布图层</button>
                <button id="display" onClick={this.showMap}>显示地图</button>
            </div>
        );
    }
}

export default ToolBox;