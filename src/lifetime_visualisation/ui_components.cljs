(ns lifetime-visualisation.ui-components
  (:require ["reactstrap" :as rs]
            [reagent.core :as ra]))

(def row (ra/adapt-react-class rs/Row))
(def col (ra/adapt-react-class rs/Col))
(def dropdown-menu (ra/adapt-react-class rs/DropdownMenu))
(def dropdown-item (ra/adapt-react-class rs/DropdownItem))
(def container (ra/adapt-react-class rs/Container))
(def popover (ra/adapt-react-class rs/Popover))
(def popover-body (ra/adapt-react-class rs/PopoverBody))
(def popover-header (ra/adapt-react-class rs/PopoverHeader))
(def form (ra/adapt-react-class rs/Form))
(def form-group (ra/adapt-react-class rs/FormGroup))
(def button (ra/adapt-react-class rs/Button))
(def input (ra/adapt-react-class rs/Input))
(def label (ra/adapt-react-class rs/Label))
(def close-button (ra/adapt-react-class rs/CloseButton))
