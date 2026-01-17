#include "MobileRT/ObjectLoader.hpp"

using ::MobileRT::ObjectLoader;

/**
 * Checks whether the object loader already loaded the scene geometry from the file.
 *
 * @return Whether the object loader already loaded the scene geometry from the file.
 */
bool ObjectLoader::isProcessed() const {
    return this->isProcessed_;
}

/**
 * The destructor.
 */
ObjectLoader::~ObjectLoader() noexcept {
}
