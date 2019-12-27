#include "MobileRT/ObjectLoader.hpp"

using ::MobileRT::ObjectLoader;

bool ObjectLoader::isProcessed() const noexcept {
    return this->isProcessed_;
}
