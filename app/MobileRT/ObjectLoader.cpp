#include "MobileRT/ObjectLoader.hpp"

using ::MobileRT::ObjectLoader;

bool ObjectLoader::isProcessed() const {
    return this->isProcessed_;
}

ObjectLoader::~ObjectLoader() {
    LOG("OBJECTLOADER DESTROYED!!!");
}
