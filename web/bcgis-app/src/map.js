import React from 'react';
import 'ol/ol.css';
import {Map, View} from 'ol';
import TileLayer from 'ol/layer/Tile';
import OSM from 'ol/source/OSM';

class MapClass extends React.Component {
    componentDidMount(){
        let map = new Map({
            target: 'map',
            layers: [
              new TileLayer({
                source: new OSM()
              })
            ],
            view: new View({
              center: [0, 0],
              zoom: 0
            })
          });
    }
    render(){
        return(
            <p id="map"></p>
        )
    }
}

export default MapClass;