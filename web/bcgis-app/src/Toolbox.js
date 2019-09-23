import React from 'react';
import ReactDOM from 'react-dom';
import MapClass from "./MapClass";
import Publish from './Publish';
import Axios from 'axios';

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

    listMaps() {
        Axios.get('http://localhost:8899/bcgis/mapservice/wms/list')
            .then(function (response) {
                console.log(response);
            })
            .catch(function (error) {
                console.log(error);
            });
    }

    render() {
        return (
            <div className="buttons">
                <button id="btn_showPublish" onClick={this.showPublish} >发布地图</button>
                <button id="btn_listMaps" onClick={this.listMaps} >地图列表</button>
                <button id="btn_showMap" onClick={this.showMap} >显示地图</button>
            </div>
        );
    }
}

export default ToolBox;