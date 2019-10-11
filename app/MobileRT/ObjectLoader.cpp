//
// Created by puscas on 30/07/17.
//

#include "MobileRT/ObjectLoader.hpp"

using ::MobileRT::ObjectLoader;

ObjectLoader::~ObjectLoader() noexcept {
    LOG("ObjectLoader DELETED");
}

bool ObjectLoader::isProcessed() const noexcept {
    return isProcessed_;
}
