import React from 'react';
import 'ol/ol.css';
import { Map, View } from 'ol';
import TileLayer from 'ol/layer/Tile';
import TileWms from 'ol/source/TileWMS';
import { fromLonLat } from 'ol/proj';

class MapClass extends React.Component {
    componentDidMount() {
        let map = new Map({
            target: 'map',
            layers: [
                new TileLayer({
                    source: new TileWms({
                        url: 'http://localhost:8070/geoserver/D/wms',
                        params: {
                            'LAYERS': 'D:D',
                            'TILED': true
                        },
                        serverType: 'geoserver',
                        transition: 0
                    })
                })
            ],
            view: new View({
                center: fromLonLat([116.5, 40]),
                zoom: 9
            })
        });
    }
    render() {
        return (
            <p id="map"></p>
        )
    }
}

export default MapClass;